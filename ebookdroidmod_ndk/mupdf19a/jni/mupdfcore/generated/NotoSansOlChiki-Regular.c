#ifndef __STRICT_ANSI__
#if defined(__linux__) || defined(__FreeBSD__) || defined(__OpenBSD__)
#if !defined(__ICC)
#define HAVE_INCBIN
#endif
#endif
#endif

#ifdef HAVE_INCBIN
const int fz_font_NotoSansOlChiki_Regular_ttf_size = 7628;
asm(".section .rodata");
asm(".global fz_font_NotoSansOlChiki_Regular_ttf");
asm(".type fz_font_NotoSansOlChiki_Regular_ttf STT_OBJECT");
asm(".size fz_font_NotoSansOlChiki_Regular_ttf, 7628");
asm(".balign 64");
asm("fz_font_NotoSansOlChiki_Regular_ttf:");
asm(".incbin \"resources/fonts/noto/NotoSansOlChiki-Regular.ttf\"");
#else
const int fz_font_NotoSansOlChiki_Regular_ttf_size = 7628;
const char fz_font_NotoSansOlChiki_Regular_ttf[] = {
0,1,0,0,0,11,0,128,0,3,0,48,79,83,47,50,122,252,106,20,0,0,1,56,0,0,0,96,
99,109,97,112,227,10,57,205,0,0,2,104,0,0,0,84,103,97,115,112,0,22,0,35,0,
0,29,188,0,0,0,16,103,108,121,102,117,109,169,198,0,0,3,40,0,0,20,104,104,
101,97,100,3,246,105,52,0,0,0,188,0,0,0,54,104,104,101,97,14,125,4,50,0,0,
0,244,0,0,0,36,104,109,116,120,233,143,21,83,0,0,1,152,0,0,0,208,108,111,
99,97,137,66,132,10,0,0,2,188,0,0,0,106,109,97,120,112,0,56,0,60,0,0,1,24,
0,0,0,32,110,97,109,101,141,106,184,241,0,0,23,144,0,0,6,10,112,111,115,116,
255,105,0,102,0,0,29,156,0,0,0,32,0,1,0,0,0,1,7,174,114,127,159,149,95,15,
60,245,0,11,8,0,0,0,0,0,204,192,74,210,0,0,0,0,210,40,203,252,0,43,255,227,
5,195,5,215,0,0,0,9,0,2,0,0,0,0,0,0,0,1,0,0,8,141,253,168,0,0,6,63,0,43,0,
22,5,195,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,52,0,1,0,0,0,52,0,59,0,3,0,0,0,
0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,148,1,144,0,5,0,0,5,154,5,51,
0,0,1,31,5,154,5,51,0,0,3,209,0,102,1,216,0,0,2,11,5,2,4,5,4,2,2,4,0,0,0,
0,0,0,0,0,0,0,0,0,0,4,0,0,71,79,79,71,0,64,0,0,254,255,8,141,253,168,0,0,
8,141,2,88,0,0,0,1,0,0,0,0,4,22,5,215,0,0,0,32,0,0,4,205,0,193,0,0,0,0,4,
20,0,0,2,20,0,0,4,147,0,100,4,31,0,96,4,27,0,92,4,43,0,115,3,221,0,100,4,
104,0,100,4,12,0,98,4,29,0,66,5,88,0,123,5,14,0,100,5,49,0,96,6,63,0,125,
5,104,0,100,5,96,0,100,4,172,0,174,5,137,0,96,4,162,0,164,5,8,0,174,5,8,0,
100,5,61,0,100,4,125,0,61,4,178,0,143,6,25,0,100,4,172,0,82,4,86,0,82,4,92,
0,164,5,59,0,43,5,109,0,82,6,25,0,100,5,61,0,100,4,152,0,74,5,211,0,61,5,
190,0,100,4,139,0,143,4,139,0,49,5,90,0,92,5,8,0,174,5,190,0,100,5,61,0,100,
5,190,0,100,2,37,0,147,2,37,0,147,2,37,0,147,4,0,0,82,2,156,0,82,4,66,0,82,
2,37,0,205,4,2,0,205,0,0,0,1,0,3,0,1,0,0,0,12,0,4,0,72,0,0,0,14,0,8,0,2,0,
6,0,0,0,13,0,32,0,160,28,127,254,255,255,255,0,0,0,0,0,13,0,32,0,160,28,80,
254,255,255,255,0,1,255,245,255,227,255,99,227,180,1,2,0,1,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,21,0,21,0,21,0,21,0,67,0,99,0,165,0,236,1,41,1,95,1,160,
1,223,2,55,2,120,2,186,2,235,3,55,3,113,3,153,3,247,4,33,4,74,4,115,4,185,
4,223,5,7,5,63,5,104,5,154,5,201,6,17,6,96,6,168,6,237,7,30,7,91,7,150,7,
208,8,14,8,89,8,129,8,189,9,12,9,91,9,114,9,136,9,174,9,205,9,218,10,19,10,
32,10,52,0,0,0,2,0,193,0,0,4,10,5,182,0,3,0,7,0,0,19,33,17,33,55,33,17,33,
193,3,73,252,183,104,2,121,253,135,5,182,250,74,104,4,230,0,0,2,0,100,255,
236,4,47,5,205,0,11,0,23,0,0,4,2,17,16,18,51,50,18,17,16,2,35,54,18,17,16,
2,35,34,2,17,16,18,51,1,90,246,239,245,238,249,243,244,159,146,146,159,159,
144,146,157,20,1,127,1,114,1,130,1,110,254,128,254,144,254,128,254,143,153,
1,31,1,57,1,53,1,33,254,229,254,197,254,193,254,231,0,0,1,0,96,255,236,3,
176,5,203,0,12,0,0,5,36,0,2,17,1,17,51,17,1,16,0,5,3,176,254,206,254,143,
173,2,160,176,253,119,1,87,1,50,20,108,1,104,2,51,1,216,254,28,1,207,252,
252,1,209,254,172,253,243,124,0,2,0,92,255,238,4,0,5,205,0,30,0,42,0,0,1,
6,6,35,34,36,53,52,54,55,55,38,53,52,54,51,50,22,21,20,6,7,1,6,21,20,22,51,
50,54,55,3,54,54,53,52,38,35,34,6,21,20,23,4,0,65,234,152,211,254,242,92,
111,240,238,158,135,141,154,83,63,254,207,162,165,136,97,157,31,225,45,37,
67,58,60,63,121,1,41,156,159,228,189,68,153,109,242,122,150,104,138,126,114,
80,172,57,254,205,162,68,121,143,108,89,2,215,45,87,44,49,62,60,45,59,88,
0,0,2,0,115,255,236,3,188,5,205,0,31,0,44,0,0,1,17,51,17,37,6,21,20,4,5,21,
36,0,53,52,54,55,55,39,38,38,53,52,54,51,50,22,21,20,6,7,7,39,54,54,53,52,
38,35,34,6,21,20,22,23,3,12,176,254,35,184,1,93,1,56,254,61,254,122,88,75,
62,49,59,68,154,120,111,158,48,38,33,115,32,42,66,47,49,63,71,56,3,166,2,
16,252,252,219,203,116,139,224,61,186,121,1,38,197,73,169,80,67,34,39,113,
66,105,147,139,101,55,100,36,32,87,31,74,31,43,58,66,45,37,82,31,0,0,2,0,
100,255,236,3,150,5,205,0,21,0,36,0,0,1,22,22,51,50,54,55,22,22,21,20,6,35,
34,2,17,16,0,37,21,6,0,3,6,21,20,18,51,50,54,53,52,39,6,35,34,38,1,63,44,
219,96,40,74,37,56,33,199,162,209,248,1,208,1,77,221,254,187,75,6,154,135,
95,106,19,37,69,88,219,3,113,125,244,36,44,93,138,64,133,184,1,92,1,24,1,
110,1,221,34,185,18,254,253,254,183,42,52,198,254,233,107,75,39,51,22,212,
0,1,0,100,255,236,4,31,5,205,0,32,0,0,1,20,2,35,34,0,17,52,18,55,23,7,6,2,
21,20,18,51,50,55,34,38,53,52,54,54,55,23,6,6,21,20,22,4,31,244,180,232,254,
213,207,131,148,39,119,145,187,161,163,76,150,222,117,225,108,47,156,167,
198,1,160,173,254,249,1,117,1,38,211,1,213,158,99,54,155,254,155,173,240,
254,238,166,194,162,127,205,132,18,162,30,169,121,115,124,0,2,0,98,255,236,
3,246,5,215,0,27,0,39,0,0,5,36,0,53,52,18,55,39,55,23,54,54,51,50,22,21,20,
6,35,34,38,39,6,6,21,20,0,5,2,38,35,34,6,7,22,22,51,50,54,53,3,76,254,195,
254,83,108,81,74,141,54,23,177,80,145,181,183,133,103,161,69,37,47,1,105,
1,69,117,98,68,55,131,25,73,99,53,62,90,20,134,1,198,255,122,1,4,79,133,78,
109,23,76,171,140,123,181,112,136,50,164,87,193,254,146,152,4,114,93,65,25,
140,91,96,68,0,1,0,66,255,236,3,246,5,205,0,39,0,0,1,7,1,55,23,54,54,51,50,
22,21,20,7,1,6,21,20,22,51,50,54,55,23,6,6,35,34,38,53,52,55,1,54,53,52,38,
35,34,6,7,2,2,108,254,172,108,154,64,151,82,144,159,181,254,130,78,148,122,
100,161,22,174,59,245,153,205,248,148,1,130,110,63,60,54,106,27,4,96,104,
1,96,105,162,92,82,137,121,121,176,254,127,78,68,117,149,130,61,37,133,174,
238,181,137,138,1,125,107,65,45,59,76,57,0,3,0,123,255,236,5,55,5,207,0,34,
0,42,0,55,0,0,1,14,2,35,32,36,53,17,52,54,55,50,22,23,23,54,54,51,50,22,21,
20,6,35,34,38,39,1,21,16,33,50,54,55,1,17,1,38,38,35,34,6,4,38,35,34,7,7,
23,22,22,51,50,54,53,4,248,5,151,253,165,254,226,254,223,151,140,57,131,62,
59,104,132,83,139,154,166,141,68,160,85,254,96,1,143,171,203,9,252,242,1,
66,102,88,25,48,59,3,104,73,58,86,92,45,38,45,96,32,60,83,1,158,110,217,107,
218,199,3,0,154,166,2,74,62,59,119,74,167,147,129,182,99,100,253,231,108,
254,231,153,128,3,10,254,82,1,147,96,64,77,5,96,116,54,41,49,73,95,70,0,2,
0,100,255,236,4,197,5,205,0,28,0,39,0,0,18,18,36,55,23,6,0,21,20,18,51,50,
54,55,34,38,53,52,54,51,50,22,21,20,0,35,32,0,17,1,54,53,52,38,35,34,6,21,
20,22,100,151,1,52,166,39,197,254,228,224,175,107,158,30,169,251,184,129,
156,195,254,222,249,254,252,254,190,3,168,25,101,90,70,91,179,3,112,1,71,
239,39,162,37,254,156,246,249,254,210,94,66,177,171,131,189,217,171,239,254,
158,1,133,1,59,254,246,70,96,94,143,103,75,100,125,0,2,0,96,255,236,4,207,
5,205,0,29,0,39,0,0,19,1,38,38,35,34,7,23,22,21,20,6,35,35,53,51,39,38,53,
52,54,51,32,0,17,16,0,33,34,0,55,22,22,51,50,18,53,52,2,39,141,2,152,17,162,
98,153,86,131,37,61,66,234,176,104,59,233,186,1,75,1,116,254,202,254,244,
217,254,217,172,3,199,132,187,217,95,41,1,145,3,84,20,58,43,201,60,58,48,
63,156,157,89,51,72,102,254,78,254,127,254,210,254,128,1,0,120,64,159,1,66,
211,144,1,26,41,0,0,2,0,125,255,236,5,195,5,205,0,11,0,23,0,0,4,0,17,16,0,
33,32,0,17,16,0,33,54,18,17,16,2,35,34,2,17,16,18,51,1,222,254,159,1,95,1,
71,1,63,1,97,254,156,254,194,235,244,242,235,238,246,246,236,20,1,139,1,104,
1,101,1,137,254,112,254,160,254,160,254,111,159,1,44,1,38,1,37,1,41,254,211,
254,223,254,222,254,208,0,1,0,100,255,236,5,12,5,205,0,47,0,0,1,2,0,35,32,
0,17,16,0,33,50,4,23,7,46,2,35,34,2,17,16,18,51,50,54,53,6,6,35,34,38,53,
52,54,51,33,21,33,34,6,21,20,22,51,50,54,55,5,12,34,254,213,246,254,230,254,
181,1,58,1,12,168,1,16,33,182,5,77,123,80,193,214,228,204,140,226,60,126,
49,128,179,191,165,1,98,254,131,85,83,92,78,62,198,70,2,111,254,212,254,169,
1,156,1,83,1,89,1,153,185,118,43,23,98,71,254,182,254,242,254,241,254,185,
238,114,25,30,187,124,142,166,156,82,70,76,90,62,35,0,0,2,0,100,255,236,5,
14,5,205,0,27,0,31,0,0,1,1,38,35,34,6,7,35,18,0,51,50,22,23,1,1,6,6,35,34,
0,3,51,22,22,51,50,55,3,33,21,33,2,14,1,211,104,184,172,218,32,183,34,1,64,
251,167,250,68,254,65,1,191,67,247,171,251,254,191,33,183,31,218,173,184,
104,80,1,125,254,131,2,219,1,213,131,231,219,1,26,1,66,157,150,254,65,254,
68,149,158,1,67,1,25,218,233,131,2,17,142,0,2,0,174,0,0,4,90,5,205,0,12,0,
21,0,0,51,17,51,21,55,22,0,21,20,0,7,39,17,19,7,17,23,54,54,53,52,38,174,
176,246,232,1,30,254,214,220,246,246,246,246,152,184,187,5,182,205,228,78,
254,178,205,210,254,177,71,225,254,35,5,31,238,254,101,236,53,233,156,159,
230,0,2,0,96,255,236,5,35,5,205,0,47,0,58,0,0,1,55,22,18,21,20,2,7,35,54,
18,53,52,2,39,7,22,22,21,16,0,35,34,38,53,1,38,38,35,34,6,7,23,22,21,20,6,
35,35,39,51,3,55,54,54,51,50,22,18,38,39,39,1,22,22,51,50,18,53,3,4,133,189,
221,137,80,199,90,146,108,83,78,49,58,254,227,219,144,221,2,62,55,136,67,
28,52,16,84,18,81,69,147,33,172,139,30,47,143,73,94,180,105,21,13,9,254,30,
13,119,61,161,171,5,4,201,93,254,86,249,224,254,110,91,108,1,138,215,143,
1,47,86,116,90,225,101,254,159,254,128,184,115,3,94,89,101,18,15,217,45,44,
66,74,158,1,102,24,39,54,103,253,167,121,39,28,253,49,36,81,1,84,244,0,0,
2,0,164,255,236,4,80,5,184,0,10,0,19,0,0,1,17,55,22,0,21,20,0,7,1,17,1,7,
17,5,54,54,53,52,38,1,84,246,225,1,37,254,219,225,254,90,1,166,246,1,2,144,
180,185,5,184,254,35,225,74,254,176,206,206,254,176,74,1,124,4,80,254,86,
225,254,92,231,49,226,159,158,229,0,0,1,0,174,255,236,4,164,5,184,0,20,0,
0,1,22,18,21,20,0,7,1,17,35,17,51,17,1,54,18,53,52,2,39,39,3,104,147,169,
254,228,150,254,108,176,176,1,148,117,137,170,147,58,5,184,154,254,125,186,
227,254,59,77,1,135,254,143,5,182,252,156,254,121,90,1,23,163,178,1,90,145,
58,0,1,0,100,255,236,4,90,5,184,0,20,0,0,1,7,6,2,21,20,18,23,1,17,51,17,35,
17,1,38,0,53,52,18,55,2,143,58,154,162,153,101,1,147,176,176,254,109,149,
254,226,173,143,5,184,58,154,254,173,176,174,254,228,74,1,135,3,100,250,74,
1,113,254,121,77,1,195,229,187,1,133,151,0,1,0,100,255,236,4,217,5,205,0,
44,0,0,1,22,22,51,50,18,53,52,2,39,39,51,23,22,18,21,16,0,35,32,0,17,22,22,
51,50,54,53,52,38,35,34,6,21,35,54,54,51,50,18,21,20,2,35,34,1,47,17,245,
123,159,214,84,68,53,199,51,62,73,254,201,242,254,248,254,190,96,188,67,125,
88,112,97,84,115,158,11,193,153,178,196,159,133,123,2,14,159,234,1,83,251,
163,1,79,137,106,115,136,254,188,166,254,192,254,89,1,192,1,131,96,113,171,
177,174,203,149,113,181,235,254,212,231,237,254,248,0,0,1,0,61,0,0,4,47,5,
205,0,20,0,0,33,35,17,1,39,1,38,38,35,34,6,7,7,39,54,54,51,50,22,23,7,3,201,
156,253,150,134,3,33,51,138,69,93,183,59,15,115,140,226,99,126,234,107,102,
3,104,252,152,96,4,105,51,55,122,77,20,111,165,97,116,132,145,0,1,0,143,255,
236,4,129,5,184,0,21,0,0,19,51,17,1,23,1,22,22,51,50,54,55,55,23,7,6,6,35,
34,38,39,55,246,155,2,107,133,252,223,50,135,73,86,178,72,15,114,31,89,225,
120,125,235,107,103,5,184,252,152,3,104,96,251,152,50,57,108,91,20,110,37,
107,118,116,131,146,0,1,0,100,255,236,5,180,5,205,0,29,0,0,1,1,54,18,53,52,
2,39,55,22,18,21,20,2,7,1,1,38,2,53,52,18,55,23,6,2,21,20,18,23,3,12,1,19,
128,95,168,96,114,155,177,221,178,254,231,254,232,178,222,171,161,115,94,
170,96,127,2,63,254,164,141,1,6,130,182,1,74,78,135,138,254,135,210,213,254,
98,153,1,94,254,162,153,1,159,212,203,1,120,146,135,74,254,177,181,133,254,
251,139,0,2,0,82,0,0,3,254,5,205,0,12,0,21,0,0,33,17,7,38,0,53,52,0,55,23,
53,51,17,1,55,17,39,6,6,21,20,22,3,78,246,217,254,211,1,44,218,246,176,254,
90,246,246,154,182,180,1,221,225,69,1,79,212,211,1,80,70,228,205,250,74,1,
170,236,1,155,238,56,231,156,154,233,0,1,0,82,255,236,3,242,5,205,0,24,0,
0,55,19,1,55,54,55,1,55,1,54,54,53,16,0,37,53,22,4,18,21,20,2,7,3,3,82,242,
1,24,38,29,36,253,143,104,2,82,26,21,254,133,254,158,246,1,186,228,240,205,
241,242,223,1,47,254,160,29,22,45,3,17,92,253,22,48,107,65,1,31,1,103,46,
172,21,226,254,146,241,229,254,162,72,1,47,254,209,0,1,0,164,255,236,4,10,
5,184,0,25,0,0,19,17,51,17,54,54,55,22,18,21,20,2,7,1,55,1,54,54,53,52,38,
39,39,6,6,7,180,176,27,164,55,202,230,245,187,254,74,102,1,92,112,126,140,
86,24,79,148,19,2,215,2,225,254,64,60,150,15,66,254,187,221,228,254,151,60,
1,116,115,254,215,66,240,153,164,224,39,11,25,238,77,0,3,0,43,255,236,5,16,
5,213,0,17,0,30,0,44,0,0,1,17,23,23,6,4,35,34,36,39,54,55,17,52,54,51,50,
22,3,17,52,38,35,34,6,21,17,22,51,50,54,5,39,6,6,35,34,38,39,7,22,22,51,50,
54,4,25,78,169,106,254,181,189,189,254,181,107,224,24,192,187,184,195,177,
106,96,98,105,103,100,49,111,1,12,129,60,161,78,78,165,56,129,53,243,132,
124,242,4,82,253,246,81,173,163,187,186,164,228,26,2,10,191,196,193,253,35,
2,19,139,102,102,139,253,237,69,38,223,129,47,55,57,45,129,61,135,126,0,2,
0,82,255,236,5,27,5,205,0,31,0,52,0,0,1,6,0,21,20,22,51,51,21,35,34,38,53,
52,54,36,55,22,4,22,21,20,6,35,35,53,51,50,54,53,52,0,3,17,51,17,7,39,22,
22,51,50,54,53,51,2,6,35,34,38,38,53,53,2,182,108,254,188,104,76,82,82,164,
196,175,1,69,112,113,1,70,174,197,164,82,82,76,104,254,183,185,164,76,241,
22,126,87,106,113,164,16,200,167,128,190,102,5,45,36,254,249,104,73,95,154,
173,149,104,220,207,32,32,207,220,104,149,173,154,95,73,104,1,11,252,132,
2,146,253,43,88,71,90,108,213,176,254,234,250,127,251,133,11,0,1,0,100,255,
236,5,180,5,205,0,45,0,0,1,6,2,21,20,22,22,23,7,38,2,53,52,62,6,55,55,1,1,
23,30,7,21,20,2,7,39,62,2,53,52,2,39,1,1,250,124,99,81,143,40,115,162,170,
8,12,30,37,49,59,92,46,67,1,24,1,25,67,46,87,62,50,37,30,12,8,170,162,114,
40,143,81,96,127,254,237,4,213,137,254,254,137,123,241,198,28,135,146,1,120,
203,42,81,64,99,90,93,87,118,42,64,254,162,1,94,64,43,111,91,95,90,99,64,
81,42,204,254,136,145,135,28,198,241,123,133,1,3,140,254,164,0,1,0,100,255,
236,4,217,5,205,0,43,0,0,1,38,38,35,34,2,21,20,18,23,23,35,39,2,17,16,0,51,
32,0,17,38,38,35,34,6,21,20,22,51,50,54,53,51,6,6,35,34,2,53,52,18,51,50,
4,14,14,248,123,160,212,85,67,52,198,50,137,1,54,243,1,9,1,65,99,186,65,125,
88,112,97,81,117,158,9,195,152,180,195,157,136,121,3,170,159,234,254,173,
251,163,254,173,133,106,116,1,40,1,73,1,64,1,168,254,62,254,126,99,110,171,
177,173,204,148,114,179,236,1,44,230,233,1,13,0,0,1,0,74,255,236,4,78,5,205,
0,27,0,0,19,1,38,38,35,34,6,7,35,54,36,51,50,4,23,1,22,22,51,50,54,55,51,
6,4,35,34,36,74,3,49,49,157,97,99,176,51,188,65,1,19,174,173,1,20,65,252,
205,47,162,96,99,175,51,189,65,254,238,175,176,254,238,1,98,3,6,102,101,120,
101,172,203,202,173,252,250,99,104,119,102,172,202,203,0,0,2,0,61,255,236,
5,150,5,205,0,21,0,33,0,0,1,17,6,7,39,54,36,51,50,4,23,3,17,54,55,23,6,4,
35,34,36,39,1,17,3,22,51,50,55,17,19,38,35,34,1,88,107,75,101,125,1,76,246,
175,1,18,112,178,95,87,101,123,254,177,245,175,254,238,112,1,98,135,151,191,
139,105,135,151,191,134,2,20,2,160,78,110,123,178,168,101,104,254,164,253,
94,69,122,123,177,169,100,104,4,86,252,222,254,245,92,35,3,37,1,10,92,0,0,
1,0,100,0,0,5,90,5,182,0,34,0,0,1,51,22,4,18,21,20,2,6,7,39,62,2,53,52,2,
39,17,35,38,0,17,52,18,54,55,23,14,2,21,20,18,23,2,135,176,133,1,11,147,110,
200,65,80,48,145,82,215,152,176,212,254,177,107,195,73,80,45,147,82,215,151,
5,182,37,218,254,207,175,146,254,235,219,32,152,23,170,214,115,191,1,49,61,
250,252,60,1,150,1,13,144,1,15,222,37,152,23,169,217,113,193,254,209,63,0,
2,0,143,255,236,4,90,5,205,0,27,0,33,0,0,1,6,6,35,34,38,39,55,17,39,54,54,
51,50,4,23,7,39,38,38,39,1,22,22,51,50,54,55,3,38,35,34,7,17,4,16,76,230,
134,126,211,103,112,129,89,224,128,162,1,25,87,78,7,10,47,30,253,186,40,133,
67,72,132,42,29,117,116,76,80,1,55,152,179,126,173,165,3,23,90,80,80,164,
145,195,17,33,86,36,252,152,66,82,95,83,3,183,69,20,253,133,0,2,0,49,255,
236,3,252,5,205,0,28,0,36,0,0,19,54,54,51,50,22,23,23,7,17,23,7,6,6,35,34,
36,39,55,23,22,23,1,38,38,35,34,6,7,19,22,22,51,50,55,55,17,123,78,230,132,
139,208,63,31,113,129,41,59,210,130,162,254,228,85,78,7,30,57,2,70,42,131,
67,72,133,41,29,49,123,62,68,40,47,4,129,156,176,142,107,50,166,252,233,90,
34,48,77,165,144,194,17,87,68,3,105,67,80,96,82,252,74,32,38,10,11,2,122,
0,2,0,92,255,236,4,246,5,203,0,35,0,47,0,0,1,6,35,34,38,53,52,54,51,50,23,
38,38,35,34,6,7,35,54,0,51,32,0,17,16,0,33,34,0,39,51,22,22,51,50,54,2,6,
21,20,22,51,50,54,53,52,38,35,4,14,73,105,150,190,190,150,105,73,54,203,134,
135,220,11,189,60,1,32,207,1,26,1,85,254,172,254,229,212,254,227,58,189,12,
220,134,131,205,193,124,125,86,85,125,124,86,1,168,49,201,155,156,200,49,
143,148,236,116,249,1,1,254,98,254,174,254,175,254,98,1,5,244,118,234,153,
2,144,125,86,87,124,126,85,86,125,0,1,0,174,0,0,4,164,5,205,0,20,0,0,33,55,
54,18,53,52,2,39,1,17,35,17,51,17,1,22,0,21,20,2,7,2,121,58,149,168,136,118,
254,108,176,176,1,148,157,1,21,174,142,58,149,1,86,178,161,1,25,91,254,120,
252,156,5,182,254,144,1,135,84,254,62,224,189,254,126,152,0,1,0,100,0,0,5,
90,5,182,0,34,0,0,1,6,2,21,20,22,22,23,7,38,38,2,53,16,0,55,51,17,54,18,53,
52,38,38,39,55,22,22,18,21,20,2,4,7,35,2,135,153,213,87,145,42,80,66,194,
115,1,78,213,176,154,213,80,147,48,80,71,197,107,147,254,247,135,176,5,6,
64,254,210,193,117,222,162,21,152,33,214,1,22,149,1,13,1,149,61,250,252,63,
1,50,188,113,214,171,24,152,35,223,254,239,143,175,254,208,217,39,0,2,0,100,
255,236,4,225,5,205,0,40,0,52,0,0,1,22,21,16,0,35,32,0,17,22,22,51,50,54,
53,52,38,35,34,6,21,35,54,54,51,50,18,21,20,2,35,34,39,22,22,51,50,18,53,
52,39,18,22,21,20,6,35,34,38,53,52,54,51,4,205,12,254,201,242,254,248,254,
190,96,188,67,125,88,112,97,84,115,158,11,193,153,178,196,159,133,123,113,
17,245,123,159,214,14,127,75,75,52,52,75,75,52,3,145,89,101,254,192,254,89,
1,192,1,131,96,113,171,177,174,203,149,113,181,235,254,212,231,237,254,248,
73,159,234,1,83,251,85,105,1,129,75,52,52,75,75,52,52,75,0,2,0,100,255,236,
5,90,5,205,0,37,0,47,0,0,1,34,36,2,53,52,54,51,50,4,18,21,20,14,5,7,7,1,1,
39,38,2,53,52,55,23,6,21,20,23,1,1,54,54,3,52,2,38,35,34,6,21,20,4,4,166,
232,254,129,221,169,157,191,1,57,186,13,29,38,54,56,81,35,67,254,224,254,
223,33,122,165,107,164,88,135,1,35,1,34,87,104,6,143,221,118,78,92,1,149,
2,117,144,1,2,153,126,175,220,254,119,223,51,96,98,88,91,71,90,28,56,1,30,
254,226,25,86,1,48,191,218,200,76,164,178,232,147,1,41,254,215,73,226,1,14,
100,1,25,174,86,74,175,220,0,1,0,147,4,184,1,145,5,205,0,11,0,0,0,22,21,20,
6,35,34,38,53,52,54,51,1,77,68,68,61,60,65,65,60,5,205,71,66,65,75,75,65,
66,71,0,0,1,0,147,255,227,1,145,0,248,0,11,0,0,36,22,21,20,6,35,34,38,53,
52,54,51,1,77,68,68,61,60,65,65,60,248,71,66,65,75,75,65,66,71,0,2,0,147,
255,227,1,145,4,102,0,11,0,23,0,0,36,22,21,20,6,35,34,38,53,52,54,51,18,22,
21,20,6,35,34,38,53,52,54,51,1,77,68,68,61,60,65,65,60,63,66,68,61,59,66,
63,62,248,71,66,65,75,75,65,66,71,3,110,72,67,64,75,74,65,66,73,0,1,0,82,
3,14,3,174,4,45,0,16,0,0,18,7,39,54,51,50,4,51,50,55,23,6,6,35,34,36,35,225,
63,80,95,159,52,1,37,28,93,60,80,36,129,72,53,254,211,36,3,139,125,47,213,
94,121,55,91,110,94,0,0,1,0,82,3,90,2,74,3,229,0,3,0,0,19,33,21,33,82,1,248,
254,8,3,229,139,0,2,0,82,255,236,3,221,5,205,0,20,0,32,0,0,19,55,19,54,54,
55,38,36,2,53,52,54,51,50,22,18,21,20,2,4,7,1,54,53,52,2,35,34,6,21,20,22,
22,82,154,135,100,228,38,93,254,228,165,177,148,125,219,125,202,254,135,142,
2,6,23,172,119,67,76,118,206,2,6,51,254,105,33,189,81,23,220,1,20,144,166,
191,175,254,200,193,231,254,146,220,8,2,104,81,136,158,1,104,111,100,102,
216,177,0,0,1,0,205,0,170,1,88,4,20,0,3,0,0,19,51,17,35,205,139,139,4,20,
252,150,0,0,2,0,205,0,170,3,53,4,20,0,3,0,7,0,0,19,51,17,35,1,51,17,35,205,
139,139,1,221,139,139,4,20,252,150,3,106,252,150,0,0,0,0,15,0,186,0,3,0,1,
4,9,0,0,0,94,0,0,0,3,0,1,4,9,0,1,0,36,0,94,0,3,0,1,4,9,0,2,0,14,0,130,0,3,
0,1,4,9,0,3,0,74,0,144,0,3,0,1,4,9,0,4,0,36,0,94,0,3,0,1,4,9,0,5,0,30,0,218,
0,3,0,1,4,9,0,6,0,30,0,248,0,3,0,1,4,9,0,7,0,68,1,22,0,3,0,1,4,9,0,8,0,42,
1,90,0,3,0,1,4,9,0,9,0,40,1,132,0,3,0,1,4,9,0,10,0,96,1,172,0,3,0,1,4,9,0,
11,0,62,2,12,0,3,0,1,4,9,0,12,0,60,2,74,0,3,0,1,4,9,0,13,2,150,2,134,0,3,
0,1,4,9,0,14,0,52,5,28,0,67,0,111,0,112,0,121,0,114,0,105,0,103,0,104,0,116,
0,32,0,50,0,48,0,49,0,51,0,32,0,71,0,111,0,111,0,103,0,108,0,101,0,32,0,73,
0,110,0,99,0,46,0,32,0,65,0,108,0,108,0,32,0,82,0,105,0,103,0,104,0,116,0,
115,0,32,0,82,0,101,0,115,0,101,0,114,0,118,0,101,0,100,0,46,0,78,0,111,0,
116,0,111,0,32,0,83,0,97,0,110,0,115,0,32,0,79,0,108,0,32,0,67,0,104,0,105,
0,107,0,105,0,82,0,101,0,103,0,117,0,108,0,97,0,114,0,77,0,111,0,110,0,111,
0,116,0,121,0,112,0,101,0,32,0,73,0,109,0,97,0,103,0,105,0,110,0,103,0,32,
0,45,0,32,0,78,0,111,0,116,0,111,0,32,0,83,0,97,0,110,0,115,0,32,0,79,0,108,
0,32,0,67,0,104,0,105,0,107,0,105,0,86,0,101,0,114,0,115,0,105,0,111,0,110,
0,32,0,49,0,46,0,48,0,51,0,32,0,117,0,104,0,78,0,111,0,116,0,111,0,83,0,97,
0,110,0,115,0,79,0,108,0,67,0,104,0,105,0,107,0,105,0,78,0,111,0,116,0,111,
0,32,0,105,0,115,0,32,0,97,0,32,0,116,0,114,0,97,0,100,0,101,0,109,0,97,0,
114,0,107,0,32,0,111,0,102,0,32,0,71,0,111,0,111,0,103,0,108,0,101,0,32,0,
73,0,110,0,99,0,46,0,77,0,111,0,110,0,111,0,116,0,121,0,112,0,101,0,32,0,
73,0,109,0,97,0,103,0,105,0,110,0,103,0,32,0,73,0,110,0,99,0,46,0,77,0,111,
0,110,0,111,0,116,0,121,0,112,0,101,0,32,0,68,0,101,0,115,0,105,0,103,0,110,
0,32,0,84,0,101,0,97,0,109,0,68,0,97,0,116,0,97,0,32,0,117,0,110,0,104,0,
105,0,110,0,116,0,101,0,100,0,46,0,32,0,68,0,101,0,115,0,105,0,103,0,110,
0,101,0,100,0,32,0,98,0,121,0,32,0,77,0,111,0,110,0,111,0,116,0,121,0,112,
0,101,0,32,0,100,0,101,0,115,0,105,0,103,0,110,0,32,0,116,0,101,0,97,0,109,
0,46,0,104,0,116,0,116,0,112,0,58,0,47,0,47,0,119,0,119,0,119,0,46,0,103,
0,111,0,111,0,103,0,108,0,101,0,46,0,99,0,111,0,109,0,47,0,103,0,101,0,116,
0,47,0,110,0,111,0,116,0,111,0,47,0,104,0,116,0,116,0,112,0,58,0,47,0,47,
0,119,0,119,0,119,0,46,0,109,0,111,0,110,0,111,0,116,0,121,0,112,0,101,0,
46,0,99,0,111,0,109,0,47,0,115,0,116,0,117,0,100,0,105,0,111,0,84,0,104,0,
105,0,115,0,32,0,70,0,111,0,110,0,116,0,32,0,83,0,111,0,102,0,116,0,119,0,
97,0,114,0,101,0,32,0,105,0,115,0,32,0,108,0,105,0,99,0,101,0,110,0,115,0,
101,0,100,0,32,0,117,0,110,0,100,0,101,0,114,0,32,0,116,0,104,0,101,0,32,
0,83,0,73,0,76,0,32,0,79,0,112,0,101,0,110,0,32,0,70,0,111,0,110,0,116,0,
32,0,76,0,105,0,99,0,101,0,110,0,115,0,101,0,44,0,32,0,86,0,101,0,114,0,115,
0,105,0,111,0,110,0,32,0,49,0,46,0,49,0,46,0,32,0,84,0,104,0,105,0,115,0,
32,0,70,0,111,0,110,0,116,0,32,0,83,0,111,0,102,0,116,0,119,0,97,0,114,0,
101,0,32,0,105,0,115,0,32,0,100,0,105,0,115,0,116,0,114,0,105,0,98,0,117,
0,116,0,101,0,100,0,32,0,111,0,110,0,32,0,97,0,110,0,32,0,34,0,65,0,83,0,
32,0,73,0,83,0,34,0,32,0,66,0,65,0,83,0,73,0,83,0,44,0,32,0,87,0,73,0,84,
0,72,0,79,0,85,0,84,0,32,0,87,0,65,0,82,0,82,0,65,0,78,0,84,0,73,0,69,0,83,
0,32,0,79,0,82,0,32,0,67,0,79,0,78,0,68,0,73,0,84,0,73,0,79,0,78,0,83,0,32,
0,79,0,70,0,32,0,65,0,78,0,89,0,32,0,75,0,73,0,78,0,68,0,44,0,32,0,101,0,
105,0,116,0,104,0,101,0,114,0,32,0,101,0,120,0,112,0,114,0,101,0,115,0,115,
0,32,0,111,0,114,0,32,0,105,0,109,0,112,0,108,0,105,0,101,0,100,0,46,0,32,
0,83,0,101,0,101,0,32,0,116,0,104,0,101,0,32,0,83,0,73,0,76,0,32,0,79,0,112,
0,101,0,110,0,32,0,70,0,111,0,110,0,116,0,32,0,76,0,105,0,99,0,101,0,110,
0,115,0,101,0,32,0,102,0,111,0,114,0,32,0,116,0,104,0,101,0,32,0,115,0,112,
0,101,0,99,0,105,0,102,0,105,0,99,0,32,0,108,0,97,0,110,0,103,0,117,0,97,
0,103,0,101,0,44,0,32,0,112,0,101,0,114,0,109,0,105,0,115,0,115,0,105,0,111,
0,110,0,115,0,32,0,97,0,110,0,100,0,32,0,108,0,105,0,109,0,105,0,116,0,97,
0,116,0,105,0,111,0,110,0,115,0,32,0,103,0,111,0,118,0,101,0,114,0,110,0,
105,0,110,0,103,0,32,0,121,0,111,0,117,0,114,0,32,0,117,0,115,0,101,0,32,
0,111,0,102,0,32,0,116,0,104,0,105,0,115,0,32,0,70,0,111,0,110,0,116,0,32,
0,83,0,111,0,102,0,116,0,119,0,97,0,114,0,101,0,46,0,104,0,116,0,116,0,112,
0,58,0,47,0,47,0,115,0,99,0,114,0,105,0,112,0,116,0,115,0,46,0,115,0,105,
0,108,0,46,0,111,0,114,0,103,0,47,0,79,0,70,0,76,0,0,0,3,0,0,0,0,0,0,255,
102,0,102,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,3,0,8,0,10,0,14,0,
7,255,255,0,15,};
#endif