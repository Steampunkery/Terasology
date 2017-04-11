/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.physics.bullet;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3Array;
import com.bulletphysics.BulletGlobals;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TFloatIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.AABB;
import org.terasology.math.VecMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.components.RigidBodyComponent;
import org.terasology.physics.components.TriggerComponent;
import org.terasology.physics.engine.CharacterCollider;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.physics.engine.PhysicsLiquidWrapper;
import org.terasology.physics.engine.PhysicsSystem;
import org.terasology.physics.engine.PhysicsWorldWrapper;
import org.terasology.physics.engine.RigidBody;
import org.terasology.physics.shapes.BoxShapeComponent;
import org.terasology.physics.shapes.CapsuleShapeComponent;
import org.terasology.physics.shapes.CylinderShapeComponent;
import org.terasology.physics.shapes.HullShapeComponent;
import org.terasology.physics.shapes.SphereShapeComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;


import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Physics engine implementation using TeraBullet (a customised version of JBullet).
 */
public class BulletPhysics implements PhysicsEngine {

    private static final Logger logger = LoggerFactory.getLogger(BulletPhysics.class);

    private final Deque<RigidBodyRequest> insertionQueue = Lists.newLinkedList();
    private final Deque<BulletRigidBody> removalQueue = Lists.newLinkedList();

    private final btCollisionDispatcher dispatcher;
    private final btBroadphaseInterface broadphase;
    private final btDiscreteDynamicsWorld discreteDynamicsWorld;
    private final BlockEntityRegistry blockEntityRegistry;
    private final PhysicsWorldWrapper wrapper;
    private final PhysicsLiquidWrapper liquidWrapper;
    private Map<EntityRef, btRigidBody> entityRigidBodies = Maps.newHashMap();
    private Map<EntityRef, BulletCharacterMoverCollider> entityColliders = Maps.newHashMap();
    private Map<EntityRef, btPairCachingGhostObject> entityTriggers = Maps.newHashMap();
    private List<PhysicsSystem.CollisionPair> collisions = new ArrayList<>();

    public BulletPhysics(WorldProvider world) {
        Bullet.init();

        broadphase = new btDbvtBroadphase();

        broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new btGhostPairCallback());
        btCollisionConfiguration defaultCollisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(defaultCollisionConfiguration);
        btSequentialImpulseConstraintSolver sequentialImpulseConstraintSolver = new btSequentialImpulseConstraintSolver();
        discreteDynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, sequentialImpulseConstraintSolver, defaultCollisionConfiguration);
        discreteDynamicsWorld.setGravity(new Vector3(0f, -15f, 0f));
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);

        //TODO: reimplement wrapper
        wrapper = new PhysicsWorldWrapper(world);
        VoxelWorldShape worldShape = new VoxelWorldShape(wrapper);

        liquidWrapper = new PhysicsLiquidWrapper(world);
        VoxelWorldShape liquidShape = new VoxelWorldShape(liquidWrapper);

        btDefaultMotionState blockMotionState = new btDefaultMotionState(new Matrix4().idt());
        btRigidBody.btRigidBodyConstructionInfo blockConsInf = new btRigidBody.btRigidBodyConstructionInfo(0, blockMotionState, worldShape, new Vector3f());
        BulletRigidBody rigidBody = new BulletRigidBody(blockConsInf);
        rigidBody.rb.setCollisionFlags(CollisionFlags.STATIC_OBJECT | rigidBody.rb.getCollisionFlags());
        short mask = (short) (~(CollisionFilterGroups.STATIC_FILTER | StandardCollisionGroup.LIQUID.getFlag()));
        discreteDynamicsWorld.addRigidBody(rigidBody.rb, combineGroups(StandardCollisionGroup.WORLD), mask);

        btRigidBody.btRigidBodyConstructionInfo liquidConsInfo = new btRigidBody.btRigidBodyConstructionInfo(0, blockMotionState, liquidShape, new Vector3f());
        BulletRigidBody liquidBody = new BulletRigidBody(liquidConsInfo);
        liquidBody.rb.setCollisionFlags(CollisionFlags.STATIC_OBJECT | rigidBody.rb.getCollisionFlags());
        discreteDynamicsWorld.addRigidBody(liquidBody.rb, combineGroups(StandardCollisionGroup.LIQUID),
                CollisionFilterGroups.SENSOR_TRIGGER);
    }

    //*****************Physics Interface methods******************\\

    @Override
    public List<PhysicsSystem.CollisionPair> getCollisionPairs() {
        List<PhysicsSystem.CollisionPair> temp = collisions;
        collisions = new ArrayList<>();
        return temp;
    }

    @Override
    public void dispose() {
        discreteDynamicsWorld.dispose();
        //discreteDynamicsWorld.destroy();
        wrapper.dispose();
        liquidWrapper.dispose();
    }

    @Override
    public short combineGroups(CollisionGroup... groups) {
        return combineGroups(Arrays.asList(groups));
    }

    @Override
    public short combineGroups(Iterable<CollisionGroup> groups) {
        short flags = 0;
        for (CollisionGroup group : groups) {
            flags |= group.getFlag();
        }
        return flags;
    }

    @Override
    public List<EntityRef> scanArea(AABB area, CollisionGroup... collisionFilter) {
        return scanArea(area, Arrays.asList(collisionFilter));
    }

    @Override
    public List<EntityRef> scanArea(AABB area, Iterable<CollisionGroup> collisionFilter) {
        // TODO: Add the aabbTest method from newer versions of bullet to TeraBullet, use that instead

        btBoxShape shape = new btBoxShape(VecMath.to(area.getExtents()));
        btGhostObject scanObject = createCollider(VecMath.to(area.getCenter()), shape, CollisionFilterGroups.SENSOR_TRIGGER,
                combineGroups(collisionFilter), CollisionFlags.NO_CONTACT_RESPONSE);
        // This in particular is overkill
        broadphase.calculateOverlappingPairs(dispatcher);
        List<EntityRef> result = Lists.newArrayList();
        for (int i = 0; i < scanObject.getNumOverlappingObjects(); ++i) {
            btCollisionObject other = scanObject.getOverlappingObject(i);
            Object userObj = other.getUserPointer();
            if (userObj instanceof EntityRef) {
                result.add((EntityRef) userObj);
            }
        }
        removeCollider(scanObject);
        return result;
    }

    @Override
    public HitResult rayTrace(org.terasology.math.geom.Vector3f from1, org.terasology.math.geom.Vector3f direction, float distance, CollisionGroup... collisionGroups) {
        return rayTrace(from1, direction, distance, Sets.newHashSet(), collisionGroups);
    }

    @Override
    public HitResult rayTrace(org.terasology.math.geom.Vector3f from1, org.terasology.math.geom.Vector3f direction, float distance, Set<EntityRef> excludedEntities,
            CollisionGroup... collisionGroups) {
        if (excludedEntities == null) {
            return rayTrace(from1, direction, distance, collisionGroups);
        }
        Vector3 to = new Vector3(VecMath.to(direction));
        Vector3 from = VecMath.to(from1);
        to.nor();
        to.scl(distance);
        //to.scale(distance);
        to.add(from);

        short filter = combineGroups(collisionGroups);

        // lookup all the collision item ids for these entities
        Set<Integer> excludedCollisionIds = Sets.newHashSet();
        for (EntityRef excludedEntity : excludedEntities) {
            if (entityRigidBodies.containsKey(excludedEntity)) {
                excludedCollisionIds.add(entityRigidBodies.get(excludedEntity).rb.getBroadphaseHandle().getUid());
            }
            if (entityColliders.containsKey(excludedEntity)) {
                excludedCollisionIds.add(entityColliders.get(excludedEntity).collider.getBroadphaseHandle().getUid());
            }
            if (entityTriggers.containsKey(excludedEntity)) {
                excludedCollisionIds.add(entityTriggers.get(excludedEntity).getBroadphaseHandle().getUid());
            }
        }
        /*CollisionWorld.ClosestRayResultWithUserDataCallback closest =
                new ClosestRayResultWithUserDataCallbackExcludingCollisionIds(from, to, excludedCollisionIds);
        closest.collisionFilterGroup = CollisionFilterGroups.ALL_FILTER;
        closest.collisionFilterMask = filter;*/

        ClosestRayResultCallback callback = new ClosestRayResultCallback();
        callback.setCollisionFilterGroup((short) btBroadphaseProxy.CollisionFilterGroups.AllFilter);
        callback.setCollisionFilterMask(filter);

        discreteDynamicsWorld.rayTest(from, to, callback);
        if (callback.hasHit()) {
            btCollisionObject collisionObject = callback.getCollisionObject();
            Vector3 hitPointWorld = new Vector3();
            callback.getHitPointWorld(hitPointWorld);

            Vector3 hitNormalWorld = new Vector3();
            callback.getHitNormalWorld(hitNormalWorld);

            if (collisionObject.userData instanceof Vector3i) { //We hit a world block

                final EntityRef entityAt = blockEntityRegistry.getEntityAt((Vector3i) collisionObject.userData);
                return new HitResult(entityAt,
                        VecMath.from(hitPointWorld),
                        VecMath.from(hitNormalWorld),
                        (Vector3i) collisionObject.userData);
            } else if (collisionObject.userData instanceof EntityRef) { //we hit an other entity
                return new HitResult((EntityRef) collisionObject.userData,
                        VecMath.from(hitPointWorld),
                        VecMath.from(hitNormalWorld));
            } else { //we hit something we don't understand, assume its nothing and log a warning
                logger.warn("Unidentified object was hit in the physics engine: {}", collisionObject.userData);
                return new HitResult();
            }
        } else { //nothing was hit
            return new HitResult();
        }
    }

    @Override
    public void update(float delta) {
        processQueuedBodies();
        applyPendingImpulsesAndForces();
        try {
            PerformanceMonitor.startActivity("Step Simulation");
            if (discreteDynamicsWorld.stepSimulation(delta, 8) != 0) {
                for (BulletCharacterMoverCollider collider : entityColliders.values()) {
                    collider.pending = false;
                }
            }
            PerformanceMonitor.endActivity();
        } catch (Exception e) {
            logger.error("Error running simulation step.", e);
        }
        collisions.addAll(getNewCollisionPairs());
    }

    @Override
    public boolean removeRigidBody(EntityRef entity) {
        BulletRigidBody rigidBody = entityRigidBodies.remove(entity);
        if (rigidBody != null) {
            removeRigidBody(rigidBody);
            // wake up this entities neighbors
            float[] radius = new float[1];
            rigidBody.rb.getCollisionShape().getBoundingSphere(new Vector3f(), radius);
            awakenArea(rigidBody.getLocation(new org.terasology.math.geom.Vector3f()), radius[0]);
            return true;
        } else {
            logger.warn("Deleting non existing rigidBody from physics engine?! Entity: {}", entity);
            return false;
        }
    }

    @Override
    public boolean updateRigidBody(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        RigidBodyComponent rb = entity.getComponent(RigidBodyComponent.class);
        BulletRigidBody rigidBody = entityRigidBodies.get(entity);

        if (location == null) {
            logger.warn("Updating rigid body of entity that has no "
                    + "LocationComponent?! Nothing is done, except log this"
                    + " warning instead. Entity: {}", entity);
            return false;
        } else if (rigidBody != null) {
            float scale = location.getWorldScale();

            if (Math.abs(rigidBody.rb.getCollisionShape().getLocalScaling(new Vector3f()).x - scale) > BulletGlobals.SIMD_EPSILON
                    || rigidBody.collidesWith != combineGroups(rb.collidesWith)) {
                removeRigidBody(rigidBody);
                newRigidBody(entity);
            } else {
                rigidBody.rb.setAngularFactor(VecMath.to(rb.angularFactor));
                rigidBody.rb.setLinearFactor(VecMath.to(rb.linearFactor));
                rigidBody.rb.setFriction(rb.friction);
            }

            return true;
        } else {
            /*
             * During the destruction of the entity it can happen that the rigged body is already destroyed while
             * the location component changes.
             * e.g. because another component that was attached via the LocationComponent gets removed.
             *
             * In such a situation it would be wrong to recreate the rigid body as it can't be updated properly after
             * the destruction of the entity.
             *
             */
            return false;
        }

        // TODO: update if mass or collision groups change
    }

    @Override
    public boolean hasRigidBody(EntityRef entity) {
        return entityRigidBodies.containsKey(entity);
    }

    @Override
    public RigidBody getRigidBody(EntityRef entity) {
        RigidBody rb = entityRigidBodies.get(entity);
        if (rb == null) {
            rb = newRigidBody(entity);
        }
        return rb;
    }

    @Override
    public boolean removeTrigger(EntityRef entity) {
        btGhostObject ghost = entityTriggers.remove(entity);
        if (ghost != null) {
            removeCollider(ghost);
            return true;
        } else {
            return false;
        }
    }

    @Override
    //TODO: update if detectGroups changed
    public boolean updateTrigger(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        btPairCachingGhostObject triggerObj = entityTriggers.get(entity);

        if (location == null) {
            logger.warn("Trying to update or create trigger of entity that has no LocationComponent?! Entity: {}", entity);
            return false;
        }
        if (triggerObj != null) {
            float scale = location.getWorldScale();
            if (Math.abs(triggerObj.getCollisionShape().getLocalScaling(new Vector3f()).x - scale) > BulletGlobals.SIMD_EPSILON) {
                discreteDynamicsWorld.removeCollisionObject(triggerObj);
                newTrigger(entity);
            } else {
                Quat4f worldRotation = VecMath.to(location.getWorldRotation());
                Vector3f worldPosition = VecMath.to(location.getWorldPosition());
                triggerObj.setWorldTransform(new Transform(new Matrix4f(worldRotation, worldPosition, 1.0f)));
            }
            return true;
        } else {
            newTrigger(entity);
            return false;
        }
    }

    @Override
    public boolean hasTrigger(EntityRef entity) {
        return entityTriggers.containsKey(entity);
    }

    @Override
    public boolean removeCharacterCollider(EntityRef entity) {
        BulletCharacterMoverCollider toRemove = entityColliders.remove(entity);
        if (toRemove == null) {
            logger.warn("Trying to remove CharacterCollider of entity that has "
                    + "no CharacterCollider in the physics engine. Entity: {}", entity);
            return false;
        } else {
            removeCollider(toRemove.collider);
            return true;
        }
    }

    @Override
    public CharacterCollider getCharacterCollider(EntityRef entity) {
        CharacterCollider cc = entityColliders.get(entity);
        if (cc == null) {
            cc = createCharacterCollider(entity);
        }
        return cc;
    }

    @Override
    public boolean hasCharacterCollider(EntityRef entity) {
        return entityColliders.containsKey(entity);
    }

    @Override
    public Set<EntityRef> getPhysicsEntities() {
        return ImmutableSet.copyOf(entityRigidBodies.keySet());
    }

    @Override
    public Iterator<EntityRef> physicsEntitiesIterator() {
        return entityRigidBodies.keySet().iterator();
    }

    @Override
    public void awakenArea(org.terasology.math.geom.Vector3f pos, float radius) {
        Vector3f min = new Vector3f(VecMath.to(pos));
        min.sub(new Vector3f(0.6f, 0.6f, 0.6f));
        Vector3f max = new Vector3f(VecMath.to(pos));
        max.add(new Vector3f(0.6f, 0.6f, 0.6f));
        discreteDynamicsWorld.awakenRigidBodiesInArea(min, max);
    }

    @Override
    public float getEpsilon() {
        //TODO: figure out how access this from libgdx
        return 1.1920929E-7F;
    }

    //*******************Private helper methods**************************\\

    /**
     * Creates a new trigger.
     *
     * @param entity the entity to create a trigger for.
     */
    private boolean newTrigger(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        TriggerComponent trigger = entity.getComponent(TriggerComponent.class);
        ConvexShape shape = getShapeFor(entity);
        if (shape != null && location != null && trigger != null) {
            float scale = location.getWorldScale();
            shape.setLocalScaling(new Vector3f(scale, scale, scale));
            List<CollisionGroup> detectGroups = Lists.newArrayList(trigger.detectGroups);
            PairCachingGhostObject triggerObj = createCollider(
                    VecMath.to(location.getWorldPosition()),
                    shape,
                    StandardCollisionGroup.SENSOR.getFlag(),
                    combineGroups(detectGroups),
                    CollisionFlags.NO_CONTACT_RESPONSE);
            triggerObj.setUserPointer(entity);
            PairCachingGhostObject oldTrigger = entityTriggers.put(entity, triggerObj);
            if (oldTrigger != null) {
                logger.warn("Creating a trigger for an entity that already has a trigger. " +
                        "Multiple trigger pre entity are not supported. Removing old one. Entity: {}", entity);
                removeCollider(oldTrigger);
                return false;
            } else {
                return true;
            }
        } else {
            logger.warn("Trying to create trigger for entity without ShapeComponent or without LocationComponent or without TriggerComponent. Entity: {}", entity);
            return false;
        }
    }

    /**
     * Creates a Collider for the given entity based on the LocationComponent
     * and CharacterMovementComponent.
     * All collision flags are set right for a character movement component.
     *
     * @param owner the entity to create the collider for.
     * @return
     */
    private CharacterCollider createCharacterCollider(EntityRef owner) {
        LocationComponent locComp = owner.getComponent(LocationComponent.class);
        CharacterMovementComponent movementComp = owner.getComponent(CharacterMovementComponent.class);
        if (locComp == null || movementComp == null) {
            throw new IllegalArgumentException("Expected an entity with a Location component and CharacterMovementComponent.");
        }
        Vector3f pos = VecMath.to(locComp.getWorldPosition());
        final float worldScale = locComp.getWorldScale();
        final float height = (movementComp.height - 2 * movementComp.radius) * worldScale;
        final float width = movementComp.radius * worldScale;
        ConvexShape shape = new CapsuleShape(width, height);
        shape.setMargin(0.1f);
        return createCustomCollider(pos, shape, movementComp.collisionGroup.getFlag(), combineGroups(movementComp.collidesWith),
                CollisionFlags.CHARACTER_OBJECT, owner);
    }

    private RigidBody newRigidBody(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        RigidBodyComponent rigidBody = entity.getComponent(RigidBodyComponent.class);
        btConvexShape shape = getShapeFor(entity);
        if (location != null && rigidBody != null && shape != null) {
            float scale = location.getWorldScale();
            shape.setLocalScaling(new Vector3f(scale, scale, scale));

            if (rigidBody.mass < 1) {
                logger.warn("RigidBodyComponent.mass is set to less than 1.0, this can lead to strange behaviour, such as the objects moving through walls. " +
                        "Entity: {}", entity);
            }
            Vector3f fallInertia = new Vector3f();
            shape.calculateLocalInertia(rigidBody.mass, fallInertia);

            btRigidBody.btRigidBodyConstructionInfo info = new btRigidBody.btRigidBodyConstructionInfo(rigidBody.mass, new EntityMotionState(entity), shape, fallInertia);
            BulletRigidBody collider = new BulletRigidBody(info);
            collider.rb.setUserPointer(entity);
            collider.rb.setAngularFactor(VecMath.to(rigidBody.angularFactor));
            collider.rb.setLinearFactor(VecMath.to(rigidBody.linearFactor));
            collider.rb.setFriction(rigidBody.friction);
            collider.collidesWith = combineGroups(rigidBody.collidesWith);
            updateKinematicSettings(rigidBody, collider);
            BulletRigidBody oldBody = entityRigidBodies.put(entity, collider);
            addRigidBody(collider, Lists.<CollisionGroup>newArrayList(rigidBody.collisionGroup), rigidBody.collidesWith);
            if (oldBody != null) {
                removeRigidBody(oldBody);
            }
            collider.setVelocity(rigidBody.velocity, rigidBody.angularVelocity);
            collider.setTransform(location.getWorldPosition(), location.getWorldRotation());
            return collider;
        } else {
            throw new IllegalArgumentException("Can only create a new rigid body for entities with a LocationComponent," +
                    " RigidBodyComponent and ShapeComponent, this entity misses at least one: " + entity);
        }
    }

    private void removeCollider(btCollisionObject collider) {
        discreteDynamicsWorld.removeCollisionObject(collider);
    }

    /**
     * Creates a new Collider. Colliders are similar to rigid bodies, except
     * that they do not respond to forces from the physics engine. They collide
     * with other objects and other objects may move if colliding with a
     * collider, but the collider itself will only respond to movement orders
     * from outside the physics engine. Colliders also detect any objects
     * colliding with them. Allowing them to be used as sensors.
     *
     * @param pos            The initial position of the collider.
     * @param shape          The shape of this collider.
     * @param groups
     * @param filters
     * @param collisionFlags
     * @param entity         The entity to associate this collider with. Can be null.
     * @return The newly created and added to the physics engine, Collider object.
     */
    private CharacterCollider createCustomCollider(Vector3f pos, btConvexShape shape, short groups, short filters, int collisionFlags, EntityRef entity) {
        if (entityColliders.containsKey(entity)) {
            entityColliders.remove(entity);
        }
        final BulletCharacterMoverCollider bulletCollider = new BulletCharacterMoverCollider(pos, shape, groups, filters, collisionFlags, entity);
        entityColliders.put(entity, bulletCollider);
        return bulletCollider;
    }

    /**
     * To make sure the state of the physics engine is constant, all changes are
     * stored and executed at the same time. This method executes the stored
     * additions and removals of bodies to and from the physics engine. It also
     * ensures that impulses requested before the body is added to the engine
     * are applied after the body is added to the engine.
     */
    // TODO: None of the above is true.
    // TODO: This isn't necessary, create and remove bodies immediately
    private synchronized void processQueuedBodies() {
        while (!insertionQueue.isEmpty()) {
            RigidBodyRequest request = insertionQueue.poll();
            discreteDynamicsWorld.addRigidBody(request.body.rb, request.groups, request.filter);
        }
        while (!removalQueue.isEmpty()) {
            BulletRigidBody body = removalQueue.poll();
            discreteDynamicsWorld.removeRigidBody(body.rb);
        }
    }

    /**
     * Applies all pending impulses to the corresponding rigidBodies and clears
     * the pending impulses.
     */
    private void applyPendingImpulsesAndForces() {

        for (Map.Entry<EntityRef, BulletRigidBody> entree : entityRigidBodies.entrySet()) {
            BulletRigidBody body = entree.getValue();
            body.rb.applyCentralImpulse(body.pendingImpulse);
            body.rb.applyCentralForce(body.pendingForce);
            body.pendingImpulse.x = 0;
            body.pendingImpulse.y = 0;
            body.pendingImpulse.z = 0;

            body.pendingForce.x = 0;
            body.pendingForce.y = 0;
            body.pendingForce.z = 0;
        }
    }

    private void addRigidBody(BulletRigidBody body) {
        short filter = (short) (CollisionFilterGroups.DEFAULT_FILTER | CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.SENSOR_TRIGGER);
        insertionQueue.add(new RigidBodyRequest(body, CollisionFilterGroups.DEFAULT_FILTER, filter));
    }

    private void addRigidBody(BulletRigidBody body, List<CollisionGroup> groups, List<CollisionGroup> filter) {
        insertionQueue.add(new RigidBodyRequest(body, combineGroups(groups), combineGroups(filter)));
    }

    private void addRigidBody(BulletRigidBody body, short groups, short filter) {
        insertionQueue.add(new RigidBodyRequest(body, groups, (short) (filter | CollisionFilterGroups.SENSOR_TRIGGER)));
    }

    private void removeRigidBody(BulletRigidBody body) {
        removalQueue.add(body);
    }

    /**
     * Returns the shape belonging to the given entity. It currently knows 4
     * different shapes: Sphere, Capsule, Cylinder or arbitrary.
     * The shape is determined based on the shape component of the given entity.
     * If the entity has somehow got multiple shapes, only one is picked. The
     * order of priority is: Sphere, Capsule, Cylinder, arbitrary.
     * <br><br>
     * TODO: Flyweight this (take scale as parameter)
     *
     * @param entity the entity to get the shape of.
     * @return the shape of the entity, ready to be used by Bullet.
     */
    private btConvexShape getShapeFor(EntityRef entity) {
        BoxShapeComponent box = entity.getComponent(BoxShapeComponent.class);
        if (box != null) {
            Vector3f halfExtents = new Vector3f(box.extents);
            halfExtents.scale(0.5f);
            return new btBoxShape(VecMath.to(halfExtents));
        }
        SphereShapeComponent sphere = entity.getComponent(SphereShapeComponent.class);
        if (sphere != null) {
            return new btSphereShape(sphere.radius);
        }
        CapsuleShapeComponent capsule = entity.getComponent(CapsuleShapeComponent.class);
        if (capsule != null) {
            return new btCapsuleShape(capsule.radius, capsule.height);
        }
        CylinderShapeComponent cylinder = entity.getComponent(CylinderShapeComponent.class);
        if (cylinder != null) {
            return new btCylinderShape(new Vector3f(cylinder.radius, 0.5f * cylinder.height, cylinder.radius));
        }
        HullShapeComponent hull = entity.getComponent(HullShapeComponent.class);
        if (hull != null) {
            FloatBuffer buffer = FloatBuffer.allocate( hull.sourceMesh.getVertices().size());
            //ObjectArrayList<Vector3f> verts = new ObjectArrayList<>();
            TFloatIterator iterator = hull.sourceMesh.getVertices().iterator();
            while (iterator.hasNext()) {
//                Vector3f newVert = new Vector3f();
 //               newVert.x = iterator.next();
  //              newVert.y = iterator.next();
   //             newVert.z = iterator.next();
  //              verts.add(newVert);
                buffer.put(iterator.next());
                buffer.put(iterator.next());
                buffer.put(iterator.next());

            }
            return new btConvexHullShape(buffer);
        }
        CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
        if (characterMovementComponent != null) {
            return new btCapsuleShape(characterMovementComponent.radius, characterMovementComponent.height);
        }
        logger.error("Creating physics object that requires a ShapeComponent or CharacterMovementComponent, but has neither. Entity: {}", entity);
        throw new IllegalArgumentException("Creating physics object that requires a ShapeComponent or CharacterMovementComponent, but has neither. Entity: " + entity);
    }

    private void updateKinematicSettings(RigidBodyComponent rigidBody, BulletRigidBody collider) {
        if (rigidBody.kinematic) {
            collider.rb.setCollisionFlags(collider.rb.getCollisionFlags() | CollisionFlags.KINEMATIC_OBJECT);
            collider.rb.setActivationState(Collision.DISABLE_DEACTIVATION);
        } else {
            collider.rb.setCollisionFlags(collider.rb.getCollisionFlags() & ~CollisionFlags.KINEMATIC_OBJECT);
            collider.rb.setActivationState(Collision.ACTIVE_TAG);
        }
    }

    private btPairCachingGhostObject createCollider(Vector3f pos, btConvexShape shape, short groups, short filters, int collisionFlags) {

        Matrix4 startTransform =  new Matrix4(VecMath.to(pos),new Quaternion(0,0,0,1),new Vector3(1,1,1));;
        btPairCachingGhostObject result = new btPairCachingGhostObject();
        result.setWorldTransform(startTransform);
        result.setCollisionShape(shape);
        result.setCollisionFlags(collisionFlags);
        discreteDynamicsWorld.addCollisionObject(result, groups, filters);
        return result;
    }

    private Collection<? extends PhysicsSystem.CollisionPair> getNewCollisionPairs() {
        List<PhysicsSystem.CollisionPair> collisionPairs = Lists.newArrayList();

        btDiscreteDynamicsWorld world = discreteDynamicsWorld;
       // ObjectArrayList<btPersistentManifold> manifolds = new ObjectArrayList<>();
        for (btPairCachingGhostObject trigger : entityTriggers.values()) {

            EntityRef entity = (EntityRef) trigger.userData;
            btHashedOverlappingPairCache overlappingPairCache = trigger.getOverlappingPairCache();
            btBroadphasePairArray overlappingPairArray = overlappingPairCache.getOverlappingPairArray();
            for (int i = 0; i < overlappingPairCache.GetCount(); i++) {
                btBroadphasePair pair = overlappingPairArray.at(i);
                //TODO review
                if (pair.getPProxy0().getClientObject() == trigger.getUserPointer()) {

                }
            }
        }
/*
            for (btBroadphasePair initialPair : trigger.getOverlappingPairCache().getOverlappingPairArray()) {
                EntityRef otherEntity = null;
                if (initialPair.pProxy0.clientObject == trigger) {
                    if (((CollisionObject) initialPair.pProxy1.clientObject).getUserPointer() instanceof EntityRef) {
                        otherEntity = (EntityRef) ((CollisionObject) initialPair.pProxy1.clientObject).getUserPointer();
                    }
                } else {
                    if (((CollisionObject) initialPair.pProxy0.clientObject).getUserPointer() instanceof EntityRef) {
                        otherEntity = (EntityRef) ((CollisionObject) initialPair.pProxy0.clientObject).getUserPointer();
                    }
                }
                if (otherEntity == null) {
                    continue;
                }
                BroadphasePair pair = world.getPairCache().findPair(initialPair.pProxy0, initialPair.pProxy1);
                if (pair == null) {
                    continue;
                }
                manifolds.clear();
                if (pair.algorithm != null) {
                    pair.algorithm.getAllContactManifolds(manifolds);
                }
                for (PersistentManifold manifold : manifolds) {
                    for (int point = 0; point < manifold.getNumContacts(); ++point) {
                        ManifoldPoint manifoldPoint = manifold.getContactPoint(point);
                        if (manifoldPoint.getDistance() < 0) {
                            collisionPairs.add(new PhysicsSystem.CollisionPair(entity, otherEntity,
                                    VecMath.from(manifoldPoint.positionWorldOnA),
                                    VecMath.from(manifoldPoint.positionWorldOnB),
                                    manifoldPoint.getDistance(),
                                    VecMath.from(manifoldPoint.normalWorldOnB)));
                            break;
                        }
                    }
                }
            }
        }*/
        return collisionPairs;
    }

    //********************Private helper classes*********************\\
//
//    private static class ClosestRayResultWithUserDataCallbackExcludingCollisionIds extends CollisionWorld.ClosestRayResultWithUserDataCallback {
//        Set<Integer> excludedIds;
//
//        ClosestRayResultWithUserDataCallbackExcludingCollisionIds(Vector3f rayFromWorld, Vector3f rayToWorld, Set<Integer> excludedIds) {
//            super(rayFromWorld, rayToWorld);
//            this.excludedIds = excludedIds;
//        }
//
//        @Override
//        public boolean needsCollision(btBroadphaseProxy proxy0) {
//            if (excludedIds.contains(proxy0.getUid())) {
//                return false;
//            } else {
//                return super.needsCollision(proxy0);
//            }
//
//        }
//    }

    private static class RigidBodyRequest {
        final BulletRigidBody body;
        final short groups;
        final short filter;

        RigidBodyRequest(BulletRigidBody body, short groups, short filter) {
            this.body = body;
            this.groups = groups;
            this.filter = filter;
        }
    }

    private static class BulletRigidBody implements RigidBody {

        public final btRigidBody rb;
        public short collidesWith;
      //  private final Transform pooledTransform = new Transform();
        private final Vector3f pendingImpulse = new Vector3f();
        private final Vector3f pendingForce = new Vector3f();

        BulletRigidBody(btRigidBody.btRigidBodyConstructionInfo info) {
            rb = new btRigidBody(info);
        }

        @Override
        public void applyImpulse(org.terasology.math.geom.Vector3f impulse) {
            pendingImpulse.add(impulse);
        }

        @Override
        public void applyForce(org.terasology.math.geom.Vector3f force) {
            pendingForce.add(force);
        }

        @Override
        public void translate(org.terasology.math.geom.Vector3f translation) {
            rb.translate(VecMath.to(translation));
        }

        @Override
        public org.terasology.math.geom.Quat4f getOrientation(org.terasology.math.geom.Quat4f out) {
            //Quat4f vm = VecMath.to(out);
            //Quaternion vm =  rb.getOrientation();
            //rb.getOrientation(vm);
            //out.set(vm.x, vm.y, vm.z, vm.w);
            return VecMath.from(rb.getOrientation());
        }

        @Override
        public org.terasology.math.geom.Vector3f getLocation(org.terasology.math.geom.Vector3f out) {
            //Vector3f vm = VecMath.to(out);
            //rb.getCenterOfMassPosition(vm);
            //out.set(vm.x, vm.y, vm.z);

            return VecMath.from(rb.getCenterOfMassPosition());
        }

        @Override
        public org.terasology.math.geom.Vector3f getLinearVelocity(org.terasology.math.geom.Vector3f out) {
            //Vector3f vm = VecMath.to(out);
            //rb.getLinearVelocity(vm);
            //out.set(vm.x, vm.y, vm.z);
            return VecMath.from(rb.getLinearVelocity());
        }

        @Override
        public org.terasology.math.geom.Vector3f getAngularVelocity(org.terasology.math.geom.Vector3f out) {
            //Vector3f vm = VecMath.to(out);
            //rb.getAngularVelocity(vm);
            //out.set(vm.x, vm.y, vm.z);
            return VecMath.from(rb.getAngularVelocity());// out;
        }

        @Override
        public void setLinearVelocity(org.terasology.math.geom.Vector3f value) {
            rb.setLinearVelocity(VecMath.to(value));
        }

        @Override
        public void setAngularVelocity(org.terasology.math.geom.Vector3f value) {
            rb.setAngularVelocity(VecMath.to(value));
        }

        @Override
        public void setOrientation(org.terasology.math.geom.Quat4f orientation) {
            //rb.getWorldTransform(pooledTransform);
            //pooledTransform.setRotation(VecMath.to(orientation));
            //rb.proceedToTransform(pooledTransform);
            Matrix4 transform =  rb.getWorldTransform();
            transform.rotate(VecMath.to(orientation));
            rb.proceedToTransform(transform);
        }

        @Override
        public void setLocation(org.terasology.math.geom.Vector3f location) {
//            rb.getWorldTransform(pooledTransform);
 //           pooledTransform.origin.set(VecMath.to(location));
 //           rb.proceedToTransform(pooledTransform);
            Matrix4 transform =  rb.getWorldTransform();
            transform.translate(location.x,location.y,location.z);
            rb.proceedToTransform(transform);

        }

        @Override
        public void setVelocity(org.terasology.math.geom.Vector3f linear, org.terasology.math.geom.Vector3f angular) {
            rb.setLinearVelocity(VecMath.to(linear));
            rb.setAngularVelocity(VecMath.to(angular));
        }

        @Override
        public void setTransform(org.terasology.math.geom.Vector3f location, org.terasology.math.geom.Quat4f orientation) {
            Matrix4 transform =  rb.getWorldTransform();
            transform.translate(location.x,location.y,location.z);
            transform.rotate(VecMath.to(orientation));
            rb.proceedToTransform(transform);

            //rb.getWorldTransform(pooledTransform);
            //pooledTransform.origin.set(VecMath.to(location));
            //pooledTransform.setRotation(VecMath.to(orientation));
            //rb.proceedToTransform(pooledTransform);
        }

        @Override
        public boolean isActive() {
            return rb.isActive();
        }
    }

    private final class BulletCharacterMoverCollider implements CharacterCollider  {

        boolean pending = true;

        //private final Transform temp = new Transform();
        private final Vector3 tempPos = new Vector3();


        //If a class can figure out that its Collider is a BulletCollider, it
        //is allowed to gain direct access to the bullet body:
        private final btPairCachingGhostObject collider;

        private BulletCharacterMoverCollider(Vector3f pos, btConvexShape shape, List<CollisionGroup> groups, List<CollisionGroup> filters, EntityRef owner) {

            this(pos, shape, groups, filters, 0, owner);
        }

        private BulletCharacterMoverCollider(Vector3f pos, btConvexShape shape, List<CollisionGroup> groups, List<CollisionGroup> filters, int collisionFlags, EntityRef owner) {
            this(pos, shape, combineGroups(groups), combineGroups(filters), collisionFlags, owner);
        }

        private BulletCharacterMoverCollider(Vector3f pos, btConvexShape shape, short groups, short filters, int collisionFlags, EntityRef owner) {
            collider = createCollider(pos, shape, groups, filters, collisionFlags);
            collider.setUserPointer(owner);
        }

        @Override
        public boolean isPending() {
            return pending;
        }

        @Override
        public org.terasology.math.geom.Vector3f getLocation() {
            //collider.getWorldTransform(temp);
            collider.getWorldTransform().getTranslation(tempPos);
            return new org.terasology.math.geom.Vector3f(tempPos.x, tempPos.y, tempPos.z);
        }

        @Override
        public void setLocation(Vector3f loc) {
            Matrix4 matrix =  collider.getWorldTransform();
            matrix.setToTranslation(VecMath.to(loc));
            collider.setWorldTransform(matrix);
           // collider.getWorldTransform(temp);
           // temp.origin.set(VecMath.to(loc));
            //collider.setWorldTransform(temp);
        }

        @Override
        public ConvexResultCallback sweep(org.terasology.math.geom.Vector3f startPos, org.terasology.math.geom.Vector3f endPos, float allowedPenetration, float slopeFactor) {
            Matrix4 startTransform = new Matrix4(VecMath.to(startPos),new Quaternion(0, 0, 0, 1), new Vector3(1.0f,1.0f,1.0f));
            Matrix4 endTransform = new Matrix4(VecMath.to(endPos),new Quaternion(0, 0, 0, 1), new Vector3(1.0f,1.0f,1.0f));
            ConvexResultCallback callback = new ConvexResultCallback();//collider, new Vector3(0, 1, 0), slopeFactor);

            callback.setCollisionFilterGroup(collider.getBroadphaseHandle().getCollisionFilterGroup());
            callback.setCollisionFilterMask(collider.getBroadphaseHandle().getCollisionFilterMask());


//            callback.collisionFilterGroup = collider.getBroadphaseHandle().collisionFilterGroup;
  //          callback.collisionFilterMask = collider.getBroadphaseHandle().collisionFilterMask;
            //collider.convexSweepTest((btConvexShape) (collider.getCollisionShape()),startTransform, endTransform, callback, allowedPenetration);
            collider.convexSweepTest((btConvexShape)(collider.getCollisionShape()),startTransform,endTransform,callback,allowedPenetration);

            return callback;
        }
    }
}
