/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.client.indigo.renderer;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.impl.client.indigo.Indigo;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessChunkRendererRegion;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;

/**
 * Implements the main hooks for terrain rendering. Attempts to tread
 * lightly. This means we are deliberately stepping over some minor
 * optimization opportunities.
 *
 * <p>Non-Fabric renderer implementations that are looking to maximize
 * performance will likely take a much more aggressive approach.
 * For that reason, mod authors who want compatibility with advanced
 * renderers will do well to steer clear of chunk rebuild hooks unless
 * they are creating a renderer.
 *
 * <p>These hooks are intended only for the Fabric default renderer and
 * aren't expected to be present when a different renderer is being used.
 * Renderer authors are responsible for creating the hooks they need.
 * (Though they can use these as an example if they wish.)
 */
@Mixin(BuiltChunk.RebuildTask.class)
public abstract class ChunkBuilderBuiltChunkRebuildTaskMixin {
	@Final
	@Shadow
	BuiltChunk field_20839;

	@Inject(method = "render",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;iterate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;"),
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void hookChunkBuild(float cameraX, float cameraY, float cameraZ,
			BlockBufferBuilderStorage builder,
			CallbackInfoReturnable<BuiltChunk.RebuildTask.RenderData> ci,
			BuiltChunk.RebuildTask.RenderData renderData, int i, BlockPos blockPos, BlockPos blockPos2, ChunkOcclusionDataBuilder chunkOcclusionDataBuilder, ChunkRendererRegion region, MatrixStack matrixStack, Set<RenderLayer> initializedLayers, Random abstractRandom, BlockRenderManager blockRenderManager) {
		// hook just before iterating over the render chunk's chunks blocks, captures the used renderlayer set
		// accessing this.region is unsafe due to potential async cancellation, the LV has to be used!

		TerrainRenderContext renderer = TerrainRenderContext.POOL.get();
		renderer.prepare(region, field_20839, renderData, builder, initializedLayers);
		((AccessChunkRendererRegion) region).fabric_setRenderer(renderer);
	}

	/**
	 * This is the hook that actually implements the rendering API for terrain rendering.
	 *
	 * <p>It's unusual to have a @Redirect in a Fabric library, but in this case
	 * it is our explicit intention that {@link BlockRenderManager#renderBlock(BlockState, BlockPos, BlockRenderView, MatrixStack, VertexConsumer, boolean, Random)}
	 * does not execute for models that will be rendered by our renderer.
	 *
	 * <p>Any mod that wants to redirect this specific call is likely also a renderer, in which case this
	 * renderer should not be present, or the mod should probably instead be relying on the renderer API
	 * which was specifically created to provide for enhanced terrain rendering.
	 *
	 * <p>Note also that {@link BlockRenderManager#renderBlock(BlockState, BlockPos, BlockRenderView, MatrixStack, VertexConsumer, boolean, Random)}
	 * IS called if the block render type is something other than {@link BlockRenderType#MODEL}.
	 * Normally this does nothing but will allow mods to create rendering hooks that are
	 * driven off of render type. (Not recommended or encouraged, but also not prevented.)
	 */
	@Redirect(method = "render", require = 1, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;)V"))
	private void hookChunkBuildTesselate(BlockRenderManager renderManager, BlockState blockState, BlockPos blockPos, BlockRenderView blockView, MatrixStack matrix, VertexConsumer bufferBuilder, boolean checkSides, Random random) {
		if (blockState.getRenderType() == BlockRenderType.MODEL) {
			final BakedModel model = renderManager.getModel(blockState);

			if (Indigo.ALWAYS_TESSELATE_INDIGO || !((FabricBakedModel) model).isVanillaAdapter()) {
				Vec3d vec3d = blockState.getModelOffset(blockView, blockPos);
				matrix.translate(vec3d.x, vec3d.y, vec3d.z);
				((AccessChunkRendererRegion) blockView).fabric_getRenderer().tessellateBlock(blockState, blockPos, model, matrix);
				return;
			}
		}

		renderManager.renderBlock(blockState, blockPos, blockView, matrix, bufferBuilder, checkSides, random);
	}

	/**
	 * Release all references. Probably not necessary but would be $#%! to debug if it is.
	 */
	@Inject(method = "render",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModelRenderer;disableBrightnessCache()V"))
	private void hookRebuildChunkReturn(CallbackInfoReturnable<Set<BlockEntity>> ci) {
		// hook after iterating over the render chunk's chunks blocks, must be called if and only if hookChunkBuild happened

		TerrainRenderContext.POOL.get().release();
	}
}
