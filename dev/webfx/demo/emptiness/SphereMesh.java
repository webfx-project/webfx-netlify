package dev.webfx.demo.emptiness;

import dev.webfx.kit.webgl.WebGLBuffer;
import dev.webfx.kit.webgl.WebGLRenderingContext;
import dev.webfx.platform.typedarray.TypedArrayFactory;

/**
 * A unit sphere mesh (positions = normals) in WebGL buffers. Several are created with different tessellations, so
 * that small spheres on screen cost only a few triangles.
 *
 * @author Bruno Salmon
 */
final class SphereMesh {

    final WebGLBuffer positionBuffer, indexBuffer;
    final int indexCount;

    SphereMesh(WebGLRenderingContext gl, int longitudeSegments, int latitudeSegments) {
        int columns = longitudeSegments + 1;
        double[] positions = new double[columns * (latitudeSegments + 1) * 3];
        int k = 0;
        for (int i = 0; i <= latitudeSegments; i++) {
            double theta = Math.PI * i / latitudeSegments, sinTheta = Math.sin(theta), cosTheta = Math.cos(theta);
            for (int j = 0; j <= longitudeSegments; j++) {
                double phi = 2 * Math.PI * j / longitudeSegments;
                positions[k++] = sinTheta * Math.cos(phi);
                positions[k++] = cosTheta;
                positions[k++] = sinTheta * Math.sin(phi);
            }
        }
        double[] indices = new double[latitudeSegments * longitudeSegments * 6];
        k = 0;
        for (int i = 0; i < latitudeSegments; i++) {
            for (int j = 0; j < longitudeSegments; j++) {
                int a = i * columns + j, b = a + columns, c = b + 1, d = a + 1;
                // Counter-clockwise when seen from outside (so back faces can be culled)
                indices[k++] = a; indices[k++] = c; indices[k++] = b;
                indices[k++] = a; indices[k++] = d; indices[k++] = c;
            }
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
