// Draws one sphere of the scene. The mesh is a unit sphere, so each vertex position is also its normal.
// The sphere is placed directly in view space (center + radius), computed on the CPU in double precision
// relative to the camera, so that the infinite zoom never runs out of float precision.
attribute vec3 aPosition;

uniform mat4 uProjection;
uniform vec4 uSphere; // xyz = center in view space, w = radius
uniform vec4 uColor;  // rgb = color, a = opacity

varying vec3 vNormal;
varying vec3 vViewPosition;
varying vec4 vColor;

void main(void) {
    vec3 viewPosition = uSphere.xyz + uSphere.w * aPosition;
    vNormal = aPosition;
    vViewPosition = viewPosition;
    vColor = uColor;
    gl_Position = uProjection * vec4(viewPosition, 1.0);
}
