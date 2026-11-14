import re, sys, math, xml.etree.ElementTree as ET
from collections import Counter

PUML = sys.argv[1]
SVG = sys.argv[2]
MODE = sys.argv[3] if len(sys.argv) > 3 else '4'
if MODE == 'all':
    TARGETS = ('asociacion','composicion','agregacion','realizacion','generalizacion','dependencia')
elif MODE == 'struct':
    TARGETS = ('asociacion','composicion','agregacion','realizacion','generalizacion')
else:
    TARGETS = ('asociacion','composicion','agregacion','realizacion')

with open(PUML, encoding='utf-8') as f:
    plines = f.read().splitlines()

def classify(line):
    s = line.strip()
    if not s or s.startswith("'") or s.startswith('@'): return None
    if '<|--' in s: return 'generalizacion'
    if '<|..' in s: return 'realizacion'
    if '*-->' in s: return 'composicion'
    if 'o-->' in s: return 'agregacion'
    if '.[#' in s and '].>' in s: return 'dependencia'
    if '-->' in s: return 'asociacion'
    if '..>' in s: return 'dependencia'
    return None

def endpoints(line):
    s2 = re.sub(r'"[^"]*"', '', line.split(' : ')[0])
    toks = re.findall(r'edu\.unimag[\w.]*', s2)
    if len(toks) >= 2:
        return toks[0], toks[-1]
    return None, None

def normalize_qname(q):
    marker = 'edu.unimag.'
    first = q.find(marker)
    if first < 0:
        return q
    second = q.find(marker, first + len(marker))
    if second >= 0:
        return q[second:]
    return q

# Pair -> relation type, used when the SVG lacks data-source-line (Smetana/ELK).
pair_types = {}
for line in plines:
    typ = classify(line)
    if typ is None:
        continue
    a, b = endpoints(line)
    if a and b:
        pair_types[frozenset((a, b))] = typ

root = ET.parse(SVG).getroot()
def tag(e): return e.tag.split('}')[-1]
def num(s):
    try: return float(s)
    except: return None

entity_names = {}
rects = {}
for g in root.iter():
    if tag(g) != 'g' or g.get('class') not in ('entity','cluster'): continue
    eid = g.get('id')
    if not eid: continue
    q = g.get('data-qualified-name','')
    if q:
        entity_names[eid] = normalize_qname(q)
    for child in g:
        if tag(child) == 'rect':
            x=num(child.get('x')); y=num(child.get('y')); w=num(child.get('width')); h=num(child.get('height'))
            if None not in (x,y,w,h): rects[eid]=(x,y,w,h)
            break

link_type_map = {
    'association': 'asociacion',
    'composition': 'composicion',
    'aggregation': 'agregacion',
    'realization': 'realizacion',
    'generalization': 'generalizacion',
}

links = []
for g in root.iter():
    if tag(g) != 'g' or g.get('class') != 'link': continue
    src = int(g.get('data-source-line','-1'))
    typ = classify(plines[src]) if 0 <= src < len(plines) else None
    e1=g.get('data-entity-1',''); e2=g.get('data-entity-2','')
    if typ not in TARGETS:
        n1 = entity_names.get(e1); n2 = entity_names.get(e2)
        if n1 and n2:
            typ = pair_types.get(frozenset((n1, n2)))
    if typ not in TARGETS:
        typ = link_type_map.get(g.get('data-link-type',''))
    if typ not in TARGETS: continue
    path=None
    for child in g:
        if tag(child)=='path' and child.get('d') and child.get('fill')=='none':
            path=child.get('d'); break
    if path:
        text = plines[src].strip() if 0 <= src < len(plines) else ''
        links.append({'type':typ,'e1':e1,'e2':e2,'src':src+1,'d':path,'text':text})

def parse_path(d):
    toks = re.findall(r'[MCLHVZ]|-?\d+(?:\.\d+)?', d)
    i=0; cur=None; start=None; segs=[]
    def add(p):
        nonlocal cur
        if cur is not None and p!=cur: segs.append((cur,p))
        cur=p
    while i < len(toks):
        t=toks[i]; i+=1
        if t=='M':
            cur=(float(toks[i]),float(toks[i+1])); i+=2; start=cur
        elif t=='L':
            add((float(toks[i]),float(toks[i+1]))); i+=2
        elif t=='H':
            add((float(toks[i]),cur[1])); i+=1
        elif t=='V':
            add((cur[0],float(toks[i]))); i+=1
        elif t=='C':
            x1=float(toks[i]); y1=float(toks[i+1]); x2=float(toks[i+2]); y2=float(toks[i+3]); x=float(toks[i+4]); y=float(toks[i+5]); i+=6
            p0=cur
            for k in range(1,17):
                s=k/16.0; u=1-s
                add((u*u*u*p0[0]+3*u*u*s*x1+3*u*s*s*x2+s*s*s*x,
                     u*u*u*p0[1]+3*u*u*s*y1+3*u*s*s*y2+s*s*s*y))
        elif t=='Z':
            if start is not None: add(start)
        else:
            i+=1
    return segs

for l in links: l['segs']=parse_path(l['d'])

def seg_int(p1,p2,p3,p4,eps=1e-7):
    x1,y1=p1; x2,y2=p2; x3,y3=p3; x4,y4=p4
    dx1=x2-x1; dy1=y2-y1; dx2=x4-x3; dy2=y4-y3
    den=dx1*dy2-dy1*dx2
    if abs(den)<eps: return None
    t=((x3-x1)*dy2-(y3-y1)*dx2)/den
    u=((x3-x1)*dy1-(y3-y1)*dx1)/den
    if -eps<=t<=1+eps and -eps<=u<=1+eps:
        return (x1+t*dx1,y1+t*dy1)
    return None

def seg_angle(p1,p2,p3,p4):
    a1=math.atan2(p2[1]-p1[1],p2[0]-p1[0]); a2=math.atan2(p4[1]-p3[1],p4[0]-p3[0])
    d=abs(a1-a2)%math.pi
    if d>math.pi/2: d=math.pi-d
    return math.degrees(d)

def in_rect(pt,r,m=2.0):
    x,y,w,h=r
    return x-m<=pt[0]<=x+w+m and y-m<=pt[1]<=y+h+m

def near_endpoint(pt,l,tol=3.0):
    if not l['segs']: return False
    for p in (l['segs'][0][0],l['segs'][-1][1]):
        if abs(pt[0]-p[0])<=tol and abs(pt[1]-p[1])<=tol: return True
    return False

def overlap_segments(p1,p2,p3,p4,tol=1.2):
    def orient(a,b,c): return (b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0])
    if abs(orient(p1,p2,p3))>tol or abs(orient(p1,p2,p4))>tol: return False
    ax=abs(p2[0]-p1[0]); ay=abs(p2[1]-p1[1])
    if ax>=ay:
        a1,a2=sorted([p1[0],p2[0]]); b1,b2=sorted([p3[0],p4[0]])
    else:
        a1,a2=sorted([p1[1],p2[1]]); b1,b2=sorted([p3[1],p4[1]])
    return min(a2,b2)-max(a1,b1) > 1.0

counts=Counter(); samples=[]
for i in range(len(links)):
    a=links[i]
    for j in range(i+1,len(links)):
        b=links[j]
        if {a['e1'],a['e2']}=={b['e1'],b['e2']}: continue
        hit=None; pt=None
        for s1 in a['segs']:
            for s2 in b['segs']:
                if overlap_segments(s1[0],s1[1],s2[0],s2[1]):
                    hit='overlap'; pt=s1[0]; break
                r=seg_int(s1[0],s1[1],s2[0],s2[1])
                if r:
                    p=r
                    if any(eid in rects and in_rect(p,rects[eid],3.0) for eid in {a['e1'],a['e2'],b['e1'],b['e2']}): continue
                    if near_endpoint(p,a) or near_endpoint(p,b): continue
                    ang=seg_angle(s1[0],s1[1],s2[0],s2[1])
                    hit='perp' if 75<=ang<=105 else 'cross'; pt=p; break
            if hit: break
        if hit:
            key=tuple(sorted([a['type'],b['type']]))+(hit,)
            counts[key]+=1
            if len(samples)<20: samples.append((a['type'],a['src'],a['text'],b['type'],b['src'],b['text'],hit,pt))

print('PUML:', PUML)
print('SVG:', SVG)
print('Links 4 tipos:', len(links), Counter(l['type'] for l in links))
print('OVERLAP', sum(v for k,v in counts.items() if k[-1]=='overlap'))
print('PERP', sum(v for k,v in counts.items() if k[-1]=='perp'))
print('CROSS', sum(v for k,v in counts.items() if k[-1]=='cross'))
print('TOTAL', sum(counts.values()))
for k,v in counts.most_common():
    print(' ',k,v)
print('Muestras:')
for s in samples:
    print(' ',s)
