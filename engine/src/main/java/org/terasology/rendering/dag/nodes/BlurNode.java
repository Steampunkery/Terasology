/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.dag.nodes;

import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.FBOManagerSubscriber;

import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * A BlurNode takes the content of the color buffer attached to the input FBO and generates
 * a blurred version of it in the color buffer attached to the output FBO.
 */
public class BlurNode extends ConditionDependentNode implements FBOManagerSubscriber {
    private static final ResourceUrn BLUR_MATERIAL = new ResourceUrn("engine:prog.blur");

    protected float blurRadius;

    private Material blurMaterial;
    private String label;

    private BaseFBOsManager fboManager;

    private ResourceUrn inputFboUrn;
    private ResourceUrn outputFboUrn;

    private FBO inputFbo;
    private FBO outputFbo;

    /**
     * Constructs a BlurNode instance.
     *
     * @param inputFboConfig an FBOConfig instance describing the input FBO, to be retrieved from the FBO manager
     * @param outputFboConfig an FBOConfig instance describing the output FBO, to be retrieved from the FBO manager
     * @param fboManager the FBO manager from which to retrieve -both- FBOs.
     * @param blurRadius the blur radius: higher values cause higher blur. The shader's default is 16.0f.
     * @param label a String to label the instance's entry in output generated by the PerformanceMonitor
     */
    public BlurNode(Context context, FBOConfig inputFboConfig, FBOConfig outputFboConfig, BaseFBOsManager fboManager, float blurRadius, String label) {
        super(context);

        this.fboManager = fboManager;
        this.blurRadius = blurRadius;
        this.label = label;
        this.inputFboUrn = inputFboConfig.getName();
        this.outputFboUrn = outputFboConfig.getName();

        setupConditions(context);

        requiresFBO(inputFboConfig, fboManager);
        requiresFBO(outputFboConfig, fboManager);
        addDesiredStateChange(new BindFbo(outputFboUrn, fboManager));
        addDesiredStateChange(new SetViewportToSizeOf(outputFboUrn, fboManager));
        update(); // Cheeky way to initialise inputFbo, outputFbo
        fboManager.subscribe(this);

        addDesiredStateChange(new EnableMaterial(BLUR_MATERIAL));
        this.blurMaterial = getMaterial(BLUR_MATERIAL);
    }

    /**
     * This method does nothing. It is meant to be overridden by inheriting classes if needed.
     * On the other hand, there might be situations in which the blur should always occur,
     * in which case this BlurNode class is good as it is.
     */
    protected void setupConditions(Context context) { }

    /**
     * Performs the blur.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/" + label);

        // TODO: these shader-related operations should go in their own StateChange implementations
        blurMaterial.setFloat("radius", blurRadius, true);
        blurMaterial.setFloat2("texelSize", 1.0f / outputFbo.width(), 1.0f / outputFbo.height(), true);

        // TODO: binding the color buffer of an FBO should also be done in its own StateChange implementation
        inputFbo.bindTexture();

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }

    @Override
    public void update() {
        inputFbo = fboManager.get(inputFboUrn);
        outputFbo = fboManager.get(outputFboUrn);
    }
}
