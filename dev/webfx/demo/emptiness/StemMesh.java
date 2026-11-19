package dev.webfx.demo.emptiness;

import dev.webfx.kit.webgl.WebGLBuffer;
import dev.webfx.kit.webgl.WebGLRenderingContext;
import dev.webfx.platform.typedarray.TypedArrayFactory;

/**
 * An open unit cylinder mesh (radius 1 around the y-axis, from y = 0 to y = 1) used for the stems between spheres. The
 * stem vertex shader stretches it between the centers of the two spheres; its ends are hidden inside the spheres.
 *
 * @author Bruno Salmon
 */
final class StemMesh {

    final WebGLBuffer positionBuffer, indexBuffer;
    final int indexCount;

    StemMesh(WebGLRenderingContext gl, int segments) {
        double[] positions = new double[(segments + 1) * 2 * 3];
        int k = 0;
        for (int j = 0; j <= segments; j++) {
            double angle = 2 * Math.PI * j / segments, x = Math.cos(angle), z = Math.sin(angle);
            positions[k++] = x; positions[k++] = 0; positions[k++] = z; // bottom vertex (index 2j)
            positions[k++] = x; positions[k++] = 1; positions[k++] = z; // top vertex (index 2j + 1)
        }
        double[] indices = new double[segments * 6];
        k = 0;
        for (int j = 0; j < segments; j++) {
            int a = 2 * j, b = a + 1, c = a + 3, d = a + 2;
            // Counter-clockwise when seen from outside (so back faces can be culled)
            indices[k++] = a; indices[k++] = b; indices[k++] = c;
            indices[k++] = a; indices[k++] = c; indices[k++] = d;
        }
        indexCount = indices.length;
        positionBuffer = gl.createBuffer();
        gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, TypedArrayFactory.createFloat32Array(positions), gl.STATIC_DRAW);
        indexBuffer = gl.createBuffer();
        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);
        gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, TypedArrayFactory.createUint16Array(indices), gl.STATIC_DRAW);
    }
}
