// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.shapes;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.rendering.primitives.ChunkVertexFlag;

import java.util.Arrays;

/**
 * Describes the elements composing part of a block mesh. Multiple parts are patched together to define the mesh
 * for a block, or its appearance in the world.
 *
 */
public class BlockMeshPart {
    private static final float BORDER = 1f / 128f;

    private Vector3f[] vertices;
    private Vector3f[] normals;
    private Vector2f[] texCoords;
    private int[] indices;
    private int texFrames;

    public BlockMeshPart(Vector3f[] vertices, Vector3f[] normals, Vector2f[] texCoords, int[] indices) {
        this(vertices, normals, texCoords, indices, 1);
    }

    private BlockMeshPart(Vector3f[] vertices, Vector3f[] normals, Vector2f[] texCoords, int[] indices, int texFrames) {
        this.vertices = Arrays.copyOf(vertices, vertices.length);
        this.normals = Arrays.copyOf(normals, normals.length);
        this.texCoords = Arrays.copyOf(texCoords, texCoords.length);
        this.indices = Arrays.copyOf(indices, indices.length);
        this.texFrames = texFrames;
    }

    public int size() {
        return vertices.length;
    }

    public int indicesSize() {
        return indices.length;
    }

    public Vector3f getVertex(int i) {
        return vertices[i];
    }

    public Vector3f getNormal(int i) {
        return normals[i];
    }

    public Vector2f getTexCoord(int i) {
        return texCoords[i];
    }

    public int getIndex(int i) {
        return indices[i];
    }

    public int getTexFrames() {
        return texFrames;
    }

    public BlockMeshPart mapTexCoords(Vector2f offset, float width, int frames) {
        float normalisedBorder = BORDER * width;
        Vector2f[] newTexCoords = new Vector2f[texCoords.length];
        for (int i = 0; i < newTexCoords.length; ++i) {
            newTexCoords[i] = new Vector2f(offset.x + normalisedBorder + texCoords[i].x * (width - 2 * normalisedBorder),
                    offset.y + normalisedBorder + texCoords[i].y * (width - 2 * normalisedBorder));
        }
        return new BlockMeshPart(vertices, normals, newTexCoords, indices, frames);
    }

    public void appendTo(ChunkMesh chunk, int offsetX, int offsetY, int offsetZ, ChunkMesh.RenderType renderType, ChunkVertexFlag flags) {
        ChunkMesh.VertexElements elements = chunk.getVertexElements(renderType);
        for (Vector2f texCoord : texCoords) {
            elements.tex.add(texCoord.x);
            elements.tex.add(texCoord.y);
        }

        int nextIndex = elements.vertexCount;
        for (int vIdx = 0; vIdx < vertices.length; ++vIdx) {
            elements.color.add(1);
            elements.color.add(1);
            elements.color.add(1);
            elements.color.add(1);
            elements.vertices.add(vertices[vIdx].x + offsetX);
            elements.vertices.add(vertices[vIdx].y + offsetY);
            elements.vertices.add(vertices[vIdx].z + offsetZ);
            elements.normals.add(normals[vIdx].x);
            elements.normals.add(normals[vIdx].y);
            elements.normals.add(normals[vIdx].z);
            elements.flags.add(flags.getValue());
            elements.frames.add(texFrames);
        }
        elements.vertexCount += vertices.length;

        for (int index : indices) {
            elements.indices.add(index + nextIndex);
        }
    }

    public BlockMeshPart rotate(Quaternionf rotation) {
        Vector3f[] newVertices = new Vector3f[vertices.length];
        Vector3f[] newNormals = new Vector3f[normals.length];

        for (int i = 0; i < newVertices.length; ++i) {
            newVertices[i] = rotation.transform(vertices[i], new Vector3f());
            newNormals[i] = rotation.transform(normals[i], new Vector3f());
            newNormals[i].normalize();
        }

        return new BlockMeshPart(newVertices, newNormals, texCoords, indices, texFrames);
    }
}
