PlayerRenderer#render
  PlayerRenderer#setModelProperties
  (mixin.client.model.ReplacePlayerModelMixin) setReplacementModel
  RenderPlayerEvent.Pre
  (mixin.client.v1_21_1_modelanim.player.PlayerRendererMixin) RenderStateCrutches.beforeLivingRender
  LivingRenderer#render
    RenderLivingEvent.Pre
    vanilla model BS
    (mixin.client.v1_21_1_modelanim.LivingEntityRendererMixin) EntityRenderState.resetPose
    EntityModel#setupAnim
    (mixin.client.v1_21_1_modelanim.LivingEntityRendererMixin) IHumanoidAnimModel#jojo_ripples$setupHumanoidAnim
    get render type
    (mixin.client.v1_21_1_modelanim.barrage.LivingEntityRendererMixin) BarrageSwings.setupToRender
    EntityModel#renderToBuffer
    render layers
      (mixin.client.model.LivingRendererLayersMixin) @WrapWithCondition disableLayersWithCustomModel
    BarrageSwings.setupToRender
    EntityRenderer#render (leash, name tag)
  (mixin.client.v1_21_1_modelanim.player.PlayerRendererMixin) RenderStateCrutches.afterLivingRender
  RenderPlayer.Post
  (mixin.client.model.ReplacePlayerModelMixin) restoreModel


model mixins are even more voodoo tbh
