import zlib, struct
def read(path):
    d=open(path,'rb').read(); assert d[:8]==b'\x89PNG\r\n\x1a\n'
    pos=8; idat=b''; w=h=0
    while pos<len(d):
        ln=struct.unpack('>I',d[pos:pos+4])[0]; t=d[pos+4:pos+8]; c=d[pos+8:pos+8+ln]; pos+=12+ln
        if t==b'IHDR': w,h,bd,ct=struct.unpack('>IIBB',c[:10]); assert bd==8 and ct==6
        elif t==b'IDAT': idat+=c
    raw=zlib.decompress(idat); bpp=4; stride=w*4; out=bytearray(); prev=bytearray(stride); i=0
    for y in range(h):
        f=raw[i]; i+=1; line=bytearray(raw[i:i+stride]); i+=stride
        for x in range(stride):
            a=line[x-bpp] if x>=bpp else 0; b=prev[x]; c=prev[x-bpp] if x>=bpp else 0
            if f==1: line[x]=(line[x]+a)&255
            elif f==2: line[x]=(line[x]+b)&255
            elif f==3: line[x]=(line[x]+((a+b)>>1))&255
            elif f==4:
                p=a+b-c; pa,pb,pc=abs(p-a),abs(p-b),abs(p-c)
                pr=a if pa<=pb and pa<=pc else (b if pb<=pc else c); line[x]=(line[x]+pr)&255
        out+=line; prev=line
    return w,h,[[list(out[(y*w+x)*4:(y*w+x)*4+4]) for x in range(w)] for y in range(h)]
def write(path,w,h,px):
    raw=b''.join(b'\x00'+bytes(max(0,min(255,int(round(v)))) for x in range(w) for v in px[y][x]) for y in range(h))
    def chunk(t,c): return struct.pack('>I',len(c))+t+c+struct.pack('>I',zlib.crc32(t+c)&0xffffffff)
    open(path,'wb').write(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw,9))+chunk(b'IEND',b''))
def write_padded(path,w,h,px):
    """Comme write, mais complete l'image en transparent jusqu'a des multiples de 32 (exige par le client pour les modeles)."""
    W=max(32,-(-w//32)*32); H=max(32,-(-h//32)*32)
    write(path,W,H,[[(px[y][x] if y<h and x<w else [0,0,0,0]) for x in range(W)] for y in range(H)])
