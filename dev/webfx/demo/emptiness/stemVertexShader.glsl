// Draws one stem between two spheres. The mesh is an open unit cylinder around the y-axis (y from 0 to 1), stretched
// here between the two sphere centers (given in view space, like the spheres).
attribute vec3 aPosition;

uniform mat4 uProjection;
uniform vec4 uStart;   // xyz = first sphere center in view space, w = stem radius
uniform vec3 uEnd;     // second sphere center in view space
uniform vec4 uColor;   // rgb = color at the start, a = opacity
uniform vec3 uColorEnd; // color at the end

varying vec3 vNormal;
varying vec3 vViewPosition;
varying vec4 vColor;

void main(void) {
    vec3 axis = uEnd - uStart.xyz;
    vec3 d = normalize(axis);
    vec3 helper = abs(d.y) < 0.99 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    vec3 u = normalize(cross(d, helper));
    vec3 v = cross(d, u);
    vec3 normal = u * aPosition.x + v * aPosition.z;
    vec3 viewPosition = uStart.xyz + axis * aPosition.y + normal * uStart.w;
    vNormal = normal;
    vViewPosition = viewPosition;
    vColor = vec4(mix(uColor.rgb, uColorEnd, aPosition.y), uColor.a);
    gl_Position = uProjection * vec4(viewPosition, 1.0);
}
