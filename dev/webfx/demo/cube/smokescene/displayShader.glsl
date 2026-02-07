precision highp float;
precision mediump sampler2D;

varying vec2 vUv;
uniform sampler2D uTexture;

void main () {
    gl_FragColor = texture2D(uTexture, vUv);
    gl_FragColor.a = sqrt(gl_FragColor.x * gl_FragColor.y * gl_FragColor.z);
    //gl_FragColor.a = gl_FragColor.x * gl_FragColor.y * gl_FragColor.z;
}
