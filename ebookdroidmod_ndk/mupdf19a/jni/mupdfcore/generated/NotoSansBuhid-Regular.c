#ifndef __STRICT_ANSI__
#if defined(__linux__) || defined(__FreeBSD__) || defined(__OpenBSD__)
#if !defined(__ICC)
#define HAVE_INCBIN
#endif
#endif
#endif

#ifdef HAVE_INCBIN
const int fz_font_NotoSansBuhid_Regular_ttf_size = 4904;
asm(".section .rodata");
asm(".global fz_font_NotoSansBuhid_Regular_ttf");
asm(".type fz_font_NotoSansBuhid_Regular_ttf STT_OBJECT");
asm(".size fz_font_NotoSansBuhid_Regular_ttf, 4904");
asm(".balign 64");
asm("fz_font_NotoSansBuhid_Regular_ttf:");
asm(".incbin \"resources/fonts/noto/NotoSansBuhid-Regular.ttf\"");
#else
const int fz_font_NotoSansBuhid_Regular_ttf_size = 4904;
const char fz_font_NotoSansBuhid_Regular_ttf[] = {
0,1,0,0,0,14,0,128,0,3,0,96,71,68,69,70,0,98,0,92,0,0,16,216,0,0,0,42,71,
80,79,83,223,5,245,45,0,0,17,4,0,0,1,54,71,83,85,66,171,136,190,133,0,0,18,
60,0,0,0,236,79,83,47,50,123,7,107,22,0,0,1,104,0,0,0,96,99,109,97,112,24,
56,22,207,0,0,2,100,0,0,0,92,103,97,115,112,0,22,0,35,0,0,16,200,0,0,0,16,
103,108,121,102,41,162,54,89,0,0,3,16,0,0,7,156,104,101,97,100,1,219,172,
108,0,0,0,236,0,0,0,54,104,104,101,97,12,41,5,140,0,0,1,36,0,0,0,36,104,109,
116,120,201,248,8,236,0,0,1,200,0,0,0,156,108,111,99,97,32,60,34,76,0,0,2,
192,0,0,0,80,109,97,120,112,0,46,0,28,0,0,1,72,0,0,0,32,110,97,109,101,139,
88,183,228,0,0,10,172,0,0,5,250,112,111,115,116,255,184,0,50,0,0,16,168,0,
0,0,32,0,1,0,0,0,1,7,174,209,138,202,218,95,15,60,245,0,11,8,0,0,0,0,0,204,
181,105,241,0,0,0,0,210,109,241,207,251,147,254,0,8,6,6,0,0,0,0,9,0,2,0,0,
0,0,0,0,0,1,0,0,8,141,253,168,0,0,8,51,251,147,255,137,8,6,0,1,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,39,0,1,0,0,0,39,0,18,0,2,0,8,0,2,0,1,0,0,0,0,0,0,0,0,
0,0,0,0,0,2,0,1,0,4,5,156,1,144,0,5,0,0,5,154,5,51,0,0,1,31,5,154,5,51,0,
0,3,209,0,102,2,0,0,0,2,11,5,2,4,5,4,2,2,4,0,0,0,0,0,0,0,0,0,16,0,0,0,0,0,
0,71,79,79,71,0,64,0,0,254,255,8,141,253,168,0,0,8,141,2,88,0,0,0,1,0,0,0,
0,4,2,5,186,0,0,0,32,0,2,4,205,0,193,0,0,0,0,4,20,0,0,2,20,0,0,6,160,0,109,
6,160,0,106,5,0,0,109,4,244,0,113,4,203,0,190,6,113,0,113,8,51,0,102,6,137,
0,6,3,129,0,178,6,160,0,141,4,244,0,113,5,223,0,102,7,12,0,141,4,231,0,139,
6,240,0,102,6,137,0,102,6,16,0,150,4,152,0,102,0,0,253,39,0,0,251,147,2,162,
0,37,3,231,0,37,4,244,0,144,5,95,0,190,6,156,0,113,6,113,0,113,6,137,0,145,
4,32,0,178,6,160,0,141,7,12,0,102,7,193,0,141,4,231,0,121,4,231,0,121,6,240,
0,102,6,16,0,150,0,0,0,1,0,3,0,1,0,0,0,12,0,4,0,80,0,0,0,16,0,16,0,3,0,0,
0,0,0,13,0,32,0,160,23,54,23,83,254,255,255,255,0,0,0,0,0,13,0,32,0,160,23,
53,23,64,254,255,255,255,0,1,255,245,255,227,255,99,232,227,232,196,1,2,0,
1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,21,0,21,0,21,0,21,0,46,0,78,0,102,
0,124,0,145,0,172,0,205,0,234,1,0,1,28,1,49,1,78,1,109,1,123,1,161,1,188,
1,220,1,237,1,251,2,9,2,24,2,36,2,63,2,90,2,125,2,160,2,190,2,218,2,252,3,
30,3,65,3,95,3,125,3,166,3,206,0,2,0,193,0,0,4,10,5,182,0,3,0,7,0,0,19,33,
17,33,55,33,17,33,193,3,73,252,183,104,2,121,253,135,5,182,250,74,104,4,230,
0,0,1,0,109,0,0,6,129,5,182,0,9,0,0,33,19,33,55,33,3,33,1,51,1,1,37,221,254,
107,32,2,56,224,1,166,2,72,174,253,121,3,190,140,252,65,5,43,250,74,0,0,2,
0,106,254,135,6,129,5,182,0,9,0,13,0,0,33,19,33,55,33,3,33,1,51,1,5,33,7,
33,1,37,223,254,102,35,2,56,222,1,166,2,70,174,253,121,252,244,2,215,33,253,
41,3,190,140,252,65,5,43,250,74,238,139,0,1,0,109,0,0,4,115,4,74,0,9,0,0,
33,19,33,19,33,55,33,3,33,3,2,254,158,254,12,133,254,64,32,2,107,133,2,0,
207,1,223,1,223,140,254,32,253,150,0,2,0,113,0,0,4,66,4,74,0,3,0,7,0,0,1,
33,55,33,1,33,7,33,4,33,253,45,33,2,211,252,79,2,213,32,253,43,3,190,140,
252,65,139,0,1,0,190,0,0,3,180,4,139,0,7,0,0,1,51,3,33,3,35,19,33,1,82,164,
117,2,51,153,160,119,253,204,4,139,254,8,253,109,2,8,0,0,1,0,113,0,0,6,8,
4,139,0,11,0,0,55,33,19,33,55,33,3,33,7,33,3,33,145,2,45,203,253,211,33,2,
213,119,2,45,31,253,209,116,253,43,139,3,117,139,254,0,139,254,0,0,0,1,0,
102,0,0,8,6,4,74,0,15,0,0,51,19,51,3,51,19,33,3,51,1,51,1,33,19,35,3,102,
254,162,223,254,227,2,12,88,252,1,160,174,254,33,253,225,88,233,228,4,74,
252,65,2,8,253,248,3,191,251,182,2,8,253,248,0,1,0,6,0,0,6,129,5,182,0,11,
0,0,19,33,19,51,1,33,1,51,1,33,19,33,41,1,195,139,162,254,204,1,168,2,70,
174,253,121,253,43,164,254,61,3,88,2,94,250,213,5,43,250,74,2,205,0,0,1,0,
178,0,0,3,248,5,182,0,7,0,0,1,33,1,35,1,33,55,33,3,215,254,211,254,205,160,
1,49,254,170,33,3,37,5,43,250,213,5,43,139,0,0,1,0,141,0,0,6,254,4,74,0,11,
0,0,33,19,33,55,33,3,33,1,33,7,33,1,1,70,221,254,106,33,2,55,223,1,119,1,
74,2,55,33,254,98,254,179,3,190,140,252,65,3,191,140,252,66,0,1,0,113,0,0,
4,82,4,139,0,7,0,0,55,33,19,33,55,33,1,33,145,2,45,203,253,211,33,2,213,254,
244,253,43,139,3,117,139,251,117,0,0,2,0,102,0,0,5,221,5,182,0,7,0,11,0,0,
51,1,51,7,33,19,51,1,37,33,1,33,102,1,13,161,18,2,176,129,170,254,2,253,72,
2,67,1,23,253,98,4,139,73,1,116,250,74,139,3,43,0,0,1,0,141,0,0,6,92,4,74,
0,13,0,0,51,19,33,55,33,3,33,1,33,3,35,19,33,1,244,221,254,188,33,1,229,223,
1,29,1,74,2,65,254,162,222,254,249,254,179,3,190,140,252,65,3,191,251,182,
3,190,252,66,0,1,0,139,2,92,4,113,2,231,0,3,0,0,19,33,7,33,172,3,197,33,252,
59,2,231,139,0,0,2,0,102,254,0,6,213,4,156,0,11,0,15,0,0,1,19,33,19,51,3,
33,1,33,1,33,3,19,33,1,33,1,143,176,254,39,189,162,156,1,72,1,102,2,254,254,
31,253,239,176,225,1,113,1,109,254,92,254,0,2,0,3,47,253,92,4,17,251,100,
254,0,2,139,3,133,0,1,0,102,0,0,6,18,4,74,0,11,0,0,51,19,51,3,33,19,33,3,
35,19,33,1,102,254,162,223,3,33,231,254,137,116,164,159,2,211,254,164,4,74,
252,65,2,62,254,157,1,238,252,172,0,1,0,150,255,141,5,164,5,182,0,13,0,0,
5,19,33,19,33,19,33,55,33,3,33,3,33,3,4,47,158,254,24,160,254,21,157,254,
95,32,2,88,161,1,237,160,1,234,207,115,1,224,1,223,1,223,139,254,33,254,33,
253,149,0,0,1,0,102,0,0,3,164,4,74,0,5,0,0,33,33,19,51,3,33,3,131,252,227,
254,162,223,2,125,4,74,252,65,0,0,1,253,39,5,47,0,31,5,186,0,3,0,0,1,33,7,
33,253,72,2,215,33,253,41,5,186,139,0,1,251,147,254,135,254,139,255,18,0,
3,0,0,5,33,7,33,251,180,2,215,33,253,41,238,139,0,0,1,0,37,254,227,2,86,6,
0,0,3,0,0,19,1,51,1,37,1,162,143,254,92,254,227,7,29,248,227,255,255,0,37,
254,227,3,156,6,0,0,38,0,24,0,0,0,7,0,24,1,70,0,0,0,1,0,144,0,30,4,58,4,59,
0,11,0,0,37,19,33,55,33,19,51,3,33,7,33,3,1,155,114,254,131,33,1,124,98,159,
98,1,110,33,254,147,114,30,1,235,139,1,167,254,89,139,254,21,0,1,0,190,0,
0,4,251,4,139,0,11,0,0,1,51,3,33,7,33,7,33,3,35,19,33,1,82,164,117,2,51,53,
1,124,33,254,132,67,160,119,253,204,4,139,254,8,229,139,254,221,2,8,0,0,2,
0,113,0,0,6,61,4,139,0,11,0,15,0,0,55,33,19,33,55,33,3,33,7,33,3,33,1,33,
7,33,145,2,45,203,253,211,33,2,213,119,2,45,31,253,209,116,253,43,4,24,1,
180,33,254,76,139,3,117,139,254,0,139,254,0,3,129,139,0,0,2,0,113,0,0,6,8,
4,139,0,11,0,15,0,0,55,33,19,33,55,33,3,33,7,33,3,33,1,33,7,33,145,2,45,203,
253,211,33,2,213,119,2,45,31,253,209,116,253,43,3,163,1,180,33,254,76,139,
3,117,139,254,0,139,254,0,1,129,139,0,0,1,0,145,0,0,6,129,5,182,0,11,0,0,
1,33,1,33,1,33,55,33,1,33,1,33,4,7,2,122,253,121,253,43,1,47,254,61,35,2,
101,254,204,1,168,2,9,254,78,5,182,250,74,5,43,139,250,213,4,160,0,1,0,178,
0,0,3,248,5,182,0,11,0,0,1,33,3,33,3,35,19,33,19,33,55,33,3,215,254,211,154,
1,141,153,160,119,254,117,184,254,170,33,3,37,5,43,253,104,253,109,2,8,3,
35,139,0,0,1,0,141,0,0,6,254,4,74,0,15,0,0,33,19,33,55,33,3,33,1,33,7,33,
3,33,7,33,3,1,70,221,254,106,33,2,55,223,1,119,1,74,2,55,33,254,98,127,1,
140,33,254,101,158,3,190,140,252,65,3,191,140,254,148,139,254,57,0,2,0,102,
0,0,7,102,5,182,0,9,0,13,0,0,1,33,7,33,1,33,1,51,7,33,1,33,1,33,5,51,2,51,
33,254,103,254,51,252,135,1,13,161,18,2,176,252,117,2,67,1,23,253,98,5,182,
140,250,214,4,139,73,252,73,3,43,0,0,1,0,141,0,0,7,13,4,74,0,15,0,0,51,19,
33,55,33,3,33,1,33,3,33,7,33,19,33,1,244,221,254,188,33,1,229,223,1,29,1,
74,2,65,222,1,143,33,253,208,222,254,249,254,179,3,190,140,252,65,3,191,252,
65,139,3,190,252,66,0,0,1,0,121,0,0,4,160,4,139,0,13,0,0,33,19,33,55,33,19,
33,7,33,3,33,7,33,3,1,139,121,254,117,33,1,138,117,2,7,33,254,153,85,1,156,
33,254,101,121,2,9,139,1,247,139,254,148,139,253,247,0,1,0,121,0,0,4,95,4,
139,0,13,0,0,33,19,33,55,33,19,51,3,33,7,33,3,33,7,1,139,121,254,117,33,1,
138,117,159,117,1,156,33,254,101,89,1,134,33,2,9,139,1,247,254,9,139,254,
130,139,0,0,2,0,102,254,0,6,213,5,186,0,13,0,17,0,0,1,33,7,33,1,33,1,33,1,
33,3,35,19,33,37,33,1,33,1,189,2,215,33,253,205,254,233,1,72,1,102,2,254,
254,31,253,239,176,164,176,254,39,2,174,1,113,1,109,254,92,5,186,139,251,
92,4,17,251,100,254,0,2,0,139,3,133,0,2,0,150,255,141,5,164,5,182,0,13,0,
17,0,0,5,19,33,19,33,19,33,55,33,3,33,3,33,3,1,33,7,33,4,47,158,254,24,160,
254,21,157,254,95,32,2,88,161,1,237,160,1,234,207,254,83,1,140,33,254,106,
115,1,224,1,223,1,223,139,254,33,254,33,253,149,5,113,139,0,0,0,0,15,0,186,
0,3,0,1,4,9,0,0,0,94,0,0,0,3,0,1,4,9,0,1,0,30,0,94,0,3,0,1,4,9,0,2,0,14,0,
124,0,3,0,1,4,9,0,3,0,68,0,138,0,3,0,1,4,9,0,4,0,30,0,94,0,3,0,1,4,9,0,5,
0,30,0,206,0,3,0,1,4,9,0,6,0,26,0,236,0,3,0,1,4,9,0,7,0,68,1,6,0,3,0,1,4,
9,0,8,0,42,1,74,0,3,0,1,4,9,0,9,0,40,1,116,0,3,0,1,4,9,0,10,0,96,1,156,0,
3,0,1,4,9,0,11,0,62,1,252,0,3,0,1,4,9,0,12,0,60,2,58,0,3,0,1,4,9,0,13,2,150,
2,118,0,3,0,1,4,9,0,14,0,52,5,12,0,67,0,111,0,112,0,121,0,114,0,105,0,103,
0,104,0,116,0,32,0,50,0,48,0,49,0,51,0,32,0,71,0,111,0,111,0,103,0,108,0,
101,0,32,0,73,0,110,0,99,0,46,0,32,0,65,0,108,0,108,0,32,0,82,0,105,0,103,
0,104,0,116,0,115,0,32,0,82,0,101,0,115,0,101,0,114,0,118,0,101,0,100,0,46,
0,78,0,111,0,116,0,111,0,32,0,83,0,97,0,110,0,115,0,32,0,66,0,117,0,104,0,
105,0,100,0,82,0,101,0,103,0,117,0,108,0,97,0,114,0,77,0,111,0,110,0,111,
0,116,0,121,0,112,0,101,0,32,0,73,0,109,0,97,0,103,0,105,0,110,0,103,0,32,
0,45,0,32,0,78,0,111,0,116,0,111,0,32,0,83,0,97,0,110,0,115,0,32,0,66,0,117,
0,104,0,105,0,100,0,86,0,101,0,114,0,115,0,105,0,111,0,110,0,32,0,49,0,46,
0,48,0,51,0,32,0,117,0,104,0,78,0,111,0,116,0,111,0,83,0,97,0,110,0,115,0,
66,0,117,0,104,0,105,0,100,0,78,0,111,0,116,0,111,0,32,0,105,0,115,0,32,0,
97,0,32,0,116,0,114,0,97,0,100,0,101,0,109,0,97,0,114,0,107,0,32,0,111,0,
102,0,32,0,71,0,111,0,111,0,103,0,108,0,101,0,32,0,73,0,110,0,99,0,46,0,77,
0,111,0,110,0,111,0,116,0,121,0,112,0,101,0,32,0,73,0,109,0,97,0,103,0,105,
0,110,0,103,0,32,0,73,0,110,0,99,0,46,0,77,0,111,0,110,0,111,0,116,0,121,
0,112,0,101,0,32,0,68,0,101,0,115,0,105,0,103,0,110,0,32,0,84,0,101,0,97,
0,109,0,68,0,97,0,116,0,97,0,32,0,117,0,110,0,104,0,105,0,110,0,116,0,101,
0,100,0,46,0,32,0,68,0,101,0,115,0,105,0,103,0,110,0,101,0,100,0,32,0,98,
0,121,0,32,0,77,0,111,0,110,0,111,0,116,0,121,0,112,0,101,0,32,0,100,0,101,
0,115,0,105,0,103,0,110,0,32,0,116,0,101,0,97,0,109,0,46,0,104,0,116,0,116,
0,112,0,58,0,47,0,47,0,119,0,119,0,119,0,46,0,103,0,111,0,111,0,103,0,108,
0,101,0,46,0,99,0,111,0,109,0,47,0,103,0,101,0,116,0,47,0,110,0,111,0,116,
0,111,0,47,0,104,0,116,0,116,0,112,0,58,0,47,0,47,0,119,0,119,0,119,0,46,
0,109,0,111,0,110,0,111,0,116,0,121,0,112,0,101,0,46,0,99,0,111,0,109,0,47,
0,115,0,116,0,117,0,100,0,105,0,111,0,84,0,104,0,105,0,115,0,32,0,70,0,111,
0,110,0,116,0,32,0,83,0,111,0,102,0,116,0,119,0,97,0,114,0,101,0,32,0,105,
0,115,0,32,0,108,0,105,0,99,0,101,0,110,0,115,0,101,0,100,0,32,0,117,0,110,
0,100,0,101,0,114,0,32,0,116,0,104,0,101,0,32,0,83,0,73,0,76,0,32,0,79,0,
112,0,101,0,110,0,32,0,70,0,111,0,110,0,116,0,32,0,76,0,105,0,99,0,101,0,
110,0,115,0,101,0,44,0,32,0,86,0,101,0,114,0,115,0,105,0,111,0,110,0,32,0,
49,0,46,0,49,0,46,0,32,0,84,0,104,0,105,0,115,0,32,0,70,0,111,0,110,0,116,
0,32,0,83,0,111,0,102,0,116,0,119,0,97,0,114,0,101,0,32,0,105,0,115,0,32,
0,100,0,105,0,115,0,116,0,114,0,105,0,98,0,117,0,116,0,101,0,100,0,32,0,111,
0,110,0,32,0,97,0,110,0,32,0,34,0,65,0,83,0,32,0,73,0,83,0,34,0,32,0,66,0,
65,0,83,0,73,0,83,0,44,0,32,0,87,0,73,0,84,0,72,0,79,0,85,0,84,0,32,0,87,
0,65,0,82,0,82,0,65,0,78,0,84,0,73,0,69,0,83,0,32,0,79,0,82,0,32,0,67,0,79,
0,78,0,68,0,73,0,84,0,73,0,79,0,78,0,83,0,32,0,79,0,70,0,32,0,65,0,78,0,89,
0,32,0,75,0,73,0,78,0,68,0,44,0,32,0,101,0,105,0,116,0,104,0,101,0,114,0,
32,0,101,0,120,0,112,0,114,0,101,0,115,0,115,0,32,0,111,0,114,0,32,0,105,
0,109,0,112,0,108,0,105,0,101,0,100,0,46,0,32,0,83,0,101,0,101,0,32,0,116,
0,104,0,101,0,32,0,83,0,73,0,76,0,32,0,79,0,112,0,101,0,110,0,32,0,70,0,111,
0,110,0,116,0,32,0,76,0,105,0,99,0,101,0,110,0,115,0,101,0,32,0,102,0,111,
0,114,0,32,0,116,0,104,0,101,0,32,0,115,0,112,0,101,0,99,0,105,0,102,0,105,
0,99,0,32,0,108,0,97,0,110,0,103,0,117,0,97,0,103,0,101,0,44,0,32,0,112,0,
101,0,114,0,109,0,105,0,115,0,115,0,105,0,111,0,110,0,115,0,32,0,97,0,110,
0,100,0,32,0,108,0,105,0,109,0,105,0,116,0,97,0,116,0,105,0,111,0,110,0,115,
0,32,0,103,0,111,0,118,0,101,0,114,0,110,0,105,0,110,0,103,0,32,0,121,0,111,
0,117,0,114,0,32,0,117,0,115,0,101,0,32,0,111,0,102,0,32,0,116,0,104,0,105,
0,115,0,32,0,70,0,111,0,110,0,116,0,32,0,83,0,111,0,102,0,116,0,119,0,97,
0,114,0,101,0,46,0,104,0,116,0,116,0,112,0,58,0,47,0,47,0,115,0,99,0,114,
0,105,0,112,0,116,0,115,0,46,0,115,0,105,0,108,0,46,0,111,0,114,0,103,0,47,
0,79,0,70,0,76,0,0,0,3,0,0,0,0,0,0,255,181,0,50,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,1,0,3,0,8,0,10,0,14,0,7,255,255,0,15,0,1,0,0,0,12,0,0,0,34,
0,0,0,2,0,3,0,0,0,21,0,1,0,22,0,23,0,3,0,24,0,38,0,1,0,4,0,0,0,1,0,0,0,0,
0,1,0,0,0,10,0,30,0,46,0,1,68,70,76,84,0,8,0,4,0,0,0,0,255,255,0,1,0,0,0,
1,109,97,114,107,0,8,0,0,0,2,0,0,0,1,0,2,0,6,0,140,0,4,0,0,0,1,0,8,0,1,0,
12,0,18,0,1,0,40,0,52,0,1,0,1,0,22,0,1,0,9,0,8,0,10,0,12,0,13,0,14,0,16,0,
19,0,20,0,21,0,1,0,0,0,6,0,1,254,142,5,30,0,9,0,20,0,26,0,32,0,38,0,44,0,
50,0,56,0,62,0,68,0,1,2,188,4,126,0,1,4,36,3,92,0,1,2,148,6,104,0,1,5,200,
5,30,0,1,2,248,5,60,0,1,5,20,5,30,0,1,4,186,4,26,0,1,1,224,6,104,0,1,2,113,
2,168,0,4,0,0,0,1,0,8,0,1,0,12,0,18,0,1,0,38,0,50,0,1,0,1,0,23,0,1,0,8,0,
7,0,10,0,11,0,14,0,15,0,18,0,19,0,21,0,1,0,0,0,6,0,1,253,43,255,46,0,8,0,
18,0,24,0,30,0,36,0,42,0,48,0,54,0,60,0,1,2,128,2,148,0,1,3,72,255,116,0,
1,2,148,255,116,0,1,1,224,255,116,0,1,2,28,255,116,0,1,3,72,254,172,0,1,2,
188,255,116,0,1,1,244,255,116,0,0,0,1,0,0,0,10,0,30,0,44,0,1,68,70,76,84,
0,8,0,4,0,0,0,0,255,255,0,1,0,0,0,1,99,99,109,112,0,8,0,0,0,1,0,0,0,1,0,4,
0,4,0,0,0,1,0,8,0,1,0,154,0,11,0,28,0,38,0,48,0,66,0,76,0,86,0,96,0,106,0,
116,0,134,0,144,0,1,0,4,0,26,0,2,0,22,0,1,0,4,0,27,0,2,0,23,0,2,0,6,0,12,
0,28,0,2,0,22,0,29,0,2,0,23,0,1,0,4,0,30,0,2,0,22,0,1,0,4,0,31,0,2,0,23,0,
1,0,4,0,32,0,2,0,23,0,1,0,4,0,33,0,2,0,22,0,1,0,4,0,34,0,2,0,23,0,2,0,6,0,
12,0,35,0,2,0,22,0,36,0,2,0,23,0,1,0,4,0,37,0,2,0,22,0,1,0,4,0,38,0,2,0,23,
0,1,0,11,0,7,0,8,0,9,0,11,0,12,0,13,0,15,0,16,0,17,0,18,0,20,};
#endif