/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.assets.skeletalmesh;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.assets.AssetData;
import org.terasology.math.AABB;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 */
public class SkeletalMeshData implements AssetData {

    private Bone rootBone;
    private Map<String, Bone> boneLookup = Maps.newHashMap();
    private List<Bone> bones = Lists.newArrayList();
    private List<Vector2> uvs = ImmutableList.of();
    private List<BoneWeight> weights = Lists.newArrayList();
    private TIntList vertexStartWeights = new TIntArrayList();
    private TIntList vertexWeightCounts = new TIntArrayList();
    private TIntList indices = new TIntArrayList();
    private AABB staticAABB;

    public SkeletalMeshData(List<Bone> bones, List<BoneWeight> weights, List<Vector2> uvs, TIntList vertexStartWeights,
                            TIntList vertexWeightCounts, TIntList indices, AABB staticAABB) {
        for (Bone bone : bones) {
            if (bone.getParent() == null) {
                rootBone = bone;
                break;
            }
        }
        this.bones.addAll(bones);
        this.weights.addAll(weights);
        this.uvs = ImmutableList.copyOf(uvs);
        this.vertexStartWeights.addAll(vertexStartWeights);
        this.vertexWeightCounts.addAll(vertexWeightCounts);
        this.indices.addAll(indices);
        this.staticAABB = staticAABB;

        calculateNormals();
    }



    public Collection<Bone> getBones() {
        return bones;
    }

    public Bone getRootBone() {
        return rootBone;
    }

    public List<Vector3> getBindPoseVertexPositions() {
        List<Vector3> positions = Lists.newArrayListWithCapacity(bones.size());
        List<Quaternion> rotations = Lists.newArrayListWithCapacity(getBones().size());
        for (Bone bone : bones) {
            positions.add(bone.getObjectPosition());
            rotations.add(bone.getObjectRotation());
        }
        return getVertexPositions(positions, rotations);
    }

    public List<Vector3> getBindPoseVertexNormals() {
        List<Vector3> positions = Lists.newArrayListWithCapacity(bones.size());
        List<Quaternion> rotations = Lists.newArrayListWithCapacity(getBones().size());
        for (Bone bone : bones) {
            positions.add(bone.getObjectPosition());
            rotations.add(bone.getObjectRotation());
        }
        return getVertexNormals(positions, rotations);
    }

    public List<Vector3> getVertexPositions(List<Vector3> bonePositions, List<Quaternion> boneRotations) {
        List<Vector3> results = Lists.newArrayListWithCapacity(getVertexCount());
        for (int i = 0; i < vertexStartWeights.size(); ++i) {
            Vector3 vertexPos = new Vector3();
            for (int weightIndexOffset = 0; weightIndexOffset < vertexWeightCounts.get(i); ++weightIndexOffset) {
                int weightIndex = vertexStartWeights.get(i) + weightIndexOffset;
                BoneWeight weight = weights.get(weightIndex);

                Vector3 current = new Vector3(weight.getPosition()).mul(boneRotations.get(weight.getBoneIndex()));
                current.add(bonePositions.get(weight.getBoneIndex()));
                current.scl(weight.getBias());
                vertexPos.add(current);
            }
            results.add(vertexPos);
        }
        return results;
    }

    public List<Vector3> getVertexNormals(List<Vector3> bonePositions, List<Quaternion> boneRotations) {
        List<Vector3> results = Lists.newArrayListWithCapacity(getVertexCount());
        for (int i = 0; i < vertexStartWeights.size(); ++i) {
            Vector3 vertexNorm = new Vector3();
            for (int weightIndexOffset = 0; weightIndexOffset < vertexWeightCounts.get(i); ++weightIndexOffset) {
                int weightIndex = vertexStartWeights.get(i) + weightIndexOffset;
                BoneWeight weight = weights.get(weightIndex);

                Vector3 current = new Vector3(weight.getNormal()).mul(boneRotations.get(weight.getBoneIndex()));
                current.scl(weight.getBias());
                vertexNorm.add(current);
            }
            results.add(vertexNorm);
        }
        return results;
    }

    public int getVertexCount() {
        return vertexStartWeights.size();
    }

    public Bone getBone(String name) {
        return boneLookup.get(name);
    }

    public TIntList getIndices() {
        return indices;
    }

    public List<Vector2> getUVs() {
        return uvs;
    }

    public AABB getStaticAABB() {
        return staticAABB;
    }

    private void calculateNormals() {
        // TODO: Better algorithm (take into account triangle size and angles
        List<Vector3> vertices = getBindPoseVertexPositions();
        List<Vector3> normals = Lists.newArrayListWithCapacity(vertices.size());
        for (int i = 0; i < vertices.size(); ++i) {
            normals.add(new Vector3());
        }
        Vector3 v1 = new Vector3();
        Vector3 v2 = new Vector3();
        Vector3 norm = new Vector3();
        for (int i = 0; i < indices.size() / 3; ++i) {
            Vector3 baseVert = vertices.get(indices.get(i * 3));
            v1.set(vertices.get(indices.get(i * 3 + 1)));
            v1.sub(baseVert);

            v2.set(vertices.get(indices.get(i * 3 + 2)));
            v2.sub(baseVert);

            v1.nor();
            v2.nor();

            norm.set(v1);
            norm.crs(v2);

            normals.get(indices.get(i * 3)).add(norm);
            normals.get(indices.get(i * 3 + 1)).add(norm);
            normals.get(indices.get(i * 3 + 2)).add(norm);
        }

        normals.forEach(Vector3::nor);

        Quaternion inverseRot = new Quaternion();
        for (int vertIndex = 0; vertIndex < vertices.size(); ++vertIndex) {
            Vector3 normal = normals.get(vertIndex);
            for (int weightIndex = 0; weightIndex < vertexWeightCounts.get(vertIndex); ++weightIndex) {
                BoneWeight weight = weights.get(weightIndex + vertexStartWeights.get(vertIndex));

                inverseRot.set(bones.get(weight.getBoneIndex()).getObjectRotation());
                inverseRot.mul(1,-1,-1,-1);
                norm = inverseRot.transform(normal);

                //inverseRot.inverse(bones.get(weight.getBoneIndex()).getObjectRotation());
                //inverseRot.rotate(normal, norm);
                weight.setNormal(norm);
            }
        }
    }

    /**
     * Outputs the skeletal mesh as md5mesh file
     */
    public String toMD5(String shader) {
        StringBuilder sb = new StringBuilder();
        sb.append("MD5Version 10\n" +
                "commandline \"Exported from Terasology MD5SkeletonLoader\"\n" +
                "\n");
        sb.append("numJoints ").append(bones.size()).append("\n");
        sb.append("numMeshes 1\n\n");
        sb.append("joints {\n");
        for (Bone bone : bones) {

            sb.append("\t\"").append(bone.getName()).append("\" ").append(bone.getParentIndex()).append(" ( ");
            sb.append(bone.getObjectPosition().x).append(" ");
            sb.append(bone.getObjectPosition().y).append(" ");
            sb.append(bone.getObjectPosition().z).append(" ) ( ");
            Quaternion rot = new Quaternion(bone.getObjectRotation());
            rot.nor();
            if (rot.w > 0) {
                rot.x = -rot.x;
                rot.y = -rot.y;
                rot.z = -rot.z;
            }
            sb.append(rot.x).append(" ");
            sb.append(rot.y).append(" ");
            sb.append(rot.z).append(" )\n");
        }
        sb.append("}\n\n");

        sb.append("mesh {\n");
        sb.append("\tshader \"" + shader + "\"\n");
        sb.append("\tnumverts ").append(uvs.size()).append("\n");
        for (int i = 0; i < uvs.size(); i++) {
            sb.append("\tvert ").append(i).append(" (").append(uvs.get(i).x).append(" ").append(uvs.get(i).y).append(") ");
            sb.append(vertexStartWeights.get(i)).append(" ").append(vertexWeightCounts.get(i)).append("\n");
        }
        sb.append("\n");
        sb.append("\tnumtris ").append(indices.size() / 3).append("\n");
        for (int i = 0; i < indices.size() / 3; i++) {
            int i1 = indices.get(i * 3);
            int i2 = indices.get(i * 3 + 1);
            int i3 = indices.get(i * 3 + 2);
            sb.append("\ttri ").append(i).append(" ").append(i1).append(" ").append(i2).append(" ").append(i3).append("\n");
        }
        sb.append("\n");
        sb.append("\tnumweights ").append(weights.size()).append("\n");
        int meshId = 0;
        for (BoneWeight weight : weights) {
            sb.append("\tweight ").append(meshId).append(" ").append(weight.getBoneIndex()).append(" ");
            sb.append(weight.getBias()).append(" ( ");
            sb.append(weight.getPosition().x).append(" ").append(weight.getPosition().y).append(" ").append(weight.getPosition().z).append(")\n");
            meshId++;
        }
        sb.append("}\n");
        return sb.toString();
    }
}
