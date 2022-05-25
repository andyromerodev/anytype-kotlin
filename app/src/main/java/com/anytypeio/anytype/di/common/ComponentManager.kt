package com.anytypeio.anytype.di.common

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.di.feature.AddFileRelationModule
import com.anytypeio.anytype.di.feature.AddObjectRelationModule
import com.anytypeio.anytype.di.feature.AddObjectRelationValueModule
import com.anytypeio.anytype.di.feature.ArchiveModule
import com.anytypeio.anytype.di.feature.AuthModule
import com.anytypeio.anytype.di.feature.CreateAccountModule
import com.anytypeio.anytype.di.feature.CreateBookmarkModule
import com.anytypeio.anytype.di.feature.CreateDataViewViewerModule
import com.anytypeio.anytype.di.feature.CreateObjectModule
import com.anytypeio.anytype.di.feature.CreateObjectTypeModule
import com.anytypeio.anytype.di.feature.CreateSetModule
import com.anytypeio.anytype.di.feature.DataViewViewerActionModule
import com.anytypeio.anytype.di.feature.DebugSettingsModule
import com.anytypeio.anytype.di.feature.DocumentRelationModule
import com.anytypeio.anytype.di.feature.EditDataViewViewerModule
import com.anytypeio.anytype.di.feature.EditorSessionModule
import com.anytypeio.anytype.di.feature.EditorUseCaseModule
import com.anytypeio.anytype.di.feature.HomeDashboardModule
import com.anytypeio.anytype.di.feature.KeychainLoginModule
import com.anytypeio.anytype.di.feature.KeychainPhraseModule
import com.anytypeio.anytype.di.feature.LinkModule
import com.anytypeio.anytype.di.feature.LinkToObjectModule
import com.anytypeio.anytype.di.feature.LinkToObjectOrWebModule
import com.anytypeio.anytype.di.feature.MainEntryModule
import com.anytypeio.anytype.di.feature.ManageViewerModule
import com.anytypeio.anytype.di.feature.ModifyViewerSortModule
import com.anytypeio.anytype.di.feature.MoveToModule
import com.anytypeio.anytype.di.feature.ObjectAppearanceCoverModule
import com.anytypeio.anytype.di.feature.ObjectAppearanceIconModule
import com.anytypeio.anytype.di.feature.ObjectAppearancePreviewLayoutModule
import com.anytypeio.anytype.di.feature.ObjectAppearanceSettingModule
import com.anytypeio.anytype.di.feature.ObjectIconPickerBaseModule
import com.anytypeio.anytype.di.feature.ObjectIconPickerModule
import com.anytypeio.anytype.di.feature.ObjectLayoutModule
import com.anytypeio.anytype.di.feature.ObjectMenuModule
import com.anytypeio.anytype.di.feature.ObjectMenuModuleBase
import com.anytypeio.anytype.di.feature.ObjectRelationValueModule
import com.anytypeio.anytype.di.feature.ObjectSearchModule
import com.anytypeio.anytype.di.feature.ObjectSetIconPickerModule
import com.anytypeio.anytype.di.feature.ObjectSetMenuModule
import com.anytypeio.anytype.di.feature.ObjectSetModule
import com.anytypeio.anytype.di.feature.ObjectSetRecordModule
import com.anytypeio.anytype.di.feature.ObjectTypeChangeModule
import com.anytypeio.anytype.di.feature.OtherSettingsModule
import com.anytypeio.anytype.di.feature.PageNavigationModule
import com.anytypeio.anytype.di.feature.RelationDateValueModule
import com.anytypeio.anytype.di.feature.RelationTextValueModule
import com.anytypeio.anytype.di.feature.SelectAccountModule
import com.anytypeio.anytype.di.feature.SelectCoverObjectModule
import com.anytypeio.anytype.di.feature.SelectCoverObjectSetModule
import com.anytypeio.anytype.di.feature.SelectSortRelationModule
import com.anytypeio.anytype.di.feature.SetupNewAccountModule
import com.anytypeio.anytype.di.feature.SetupSelectedAccountModule
import com.anytypeio.anytype.di.feature.SplashModule
import com.anytypeio.anytype.di.feature.StartLoginModule
import com.anytypeio.anytype.di.feature.ViewerCustomizeModule
import com.anytypeio.anytype.di.feature.ViewerFilterModule
import com.anytypeio.anytype.di.feature.ViewerRelationsModule
import com.anytypeio.anytype.di.feature.ViewerSortByModule
import com.anytypeio.anytype.di.feature.ViewerSortModule
import com.anytypeio.anytype.di.feature.auth.DeletedAccountModule
import com.anytypeio.anytype.di.feature.cover.UnsplashModule
import com.anytypeio.anytype.di.feature.relations.LimitObjectTypeModule
import com.anytypeio.anytype.di.feature.relations.RelationAddToDataViewModule
import com.anytypeio.anytype.di.feature.relations.RelationAddToObjectModule
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForDataViewModule
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForObjectBlockModule
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForObjectModule
import com.anytypeio.anytype.di.feature.sets.CreateFilterModule
import com.anytypeio.anytype.di.feature.sets.ModifyFilterModule
import com.anytypeio.anytype.di.feature.sets.PickConditionModule
import com.anytypeio.anytype.di.feature.sets.SelectFilterRelationModule
import com.anytypeio.anytype.di.feature.sets.viewer.ViewerCardSizeSelectModule
import com.anytypeio.anytype.di.feature.sets.viewer.ViewerImagePreviewSelectModule
import com.anytypeio.anytype.di.feature.settings.AboutAppModule
import com.anytypeio.anytype.di.feature.settings.AccountAndDataModule
import com.anytypeio.anytype.di.feature.settings.DaggerAppearanceComponent
import com.anytypeio.anytype.di.feature.settings.LogoutWarningModule
import com.anytypeio.anytype.di.feature.settings.MainSettingsModule
import com.anytypeio.anytype.di.feature.wallpaper.WallpaperSelectModule
import com.anytypeio.anytype.di.main.MainComponent

class ComponentManager(
    private val main: MainComponent,
    private val provider: HasComponentDependencies
) {

    val mainEntryComponent = Component {
        main.mainEntryComponentBuilder().module(MainEntryModule).build()
    }

    val authComponent = Component {
        main.authComponentBuilder().authModule(AuthModule).build()
    }

    val deletedAccountComponent = Component {
        main.deletedAccountBuilder().module(DeletedAccountModule).build()
    }

    val startLoginComponent = Component {
        authComponent
            .get()
            .startLoginComponentBuilder()
            .startLoginModule(StartLoginModule)
            .build()
    }

    val createAccountComponent = Component {
        authComponent
            .get()
            .createAccountComponentBuilder()
            .createAccountModule(CreateAccountModule)
            .build()
    }

    val setupNewAccountComponent = Component {
        authComponent
            .get()
            .setupNewAccountComponentBuilder()
            .setupNewAccountModule(SetupNewAccountModule)
            .build()
    }

    val setupSelectedAccountComponent = Component {
        authComponent
            .get()
            .setupSelectedAccountComponentBuilder()
            .setupSelectedAccountModule(SetupSelectedAccountModule)
            .build()
    }

    val selectAccountComponent = Component {
        authComponent
            .get()
            .selectAccountComponentBuilder()
            .selectAccountModule(SelectAccountModule)
            .build()
    }

    val keychainLoginComponent = Component {
        authComponent
            .get()
            .keychainLoginComponentBuilder()
            .keychainLoginModule(KeychainLoginModule)
            .build()
    }

    val debugSettingsComponent = Component {
        main
            .debugSettingsBuilder()
            .module(DebugSettingsModule())
            .build()
    }

    val splashLoginComponent = Component {
        main
            .splashComponentBuilder()
            .module(SplashModule)
            .build()
    }

    val keychainPhraseComponent = Component {
        main
            .keychainPhraseComponentBuilder()
            .keychainPhraseModule(KeychainPhraseModule)
            .build()
    }

    val dashboardComponent = Component {
        main
            .homeDashboardComponentBuilder()
            .homeDashboardModule(HomeDashboardModule)
            .build()
    }

    val wallpaperSelectComponent = Component {
        main
            .wallpaperSelectComponent()
            .module(WallpaperSelectModule)
            .build()
    }

    val createObjectComponent = Component {
        main
            .createObjectComponent()
            .module(CreateObjectModule)
            .build()
    }

    val templateComponent = Component {
        main.templateComponentFactory().create()
    }

    val templateSelectComponent = Component {
        main.templateSelectComponentFactory().create()
    }

    val editorComponent = ComponentMap {
        main
            .editorComponentBuilder()
            .session(EditorSessionModule)
            .usecase(EditorUseCaseModule)
            .build()
    }

    val archiveComponent = ComponentMap {
        main.archiveComponentBuilder()
            .module(ArchiveModule)
            .build()
    }

    val linkAddComponent = Component {
        main
            .linkAddComponentBuilder()
            .linkModule(LinkModule())
            .build()
    }

    val objectIconPickerComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectIconPickerComponent()
            .base(ObjectIconPickerBaseModule)
            .module(ObjectIconPickerModule)
            .build()
    }

    val objectSetIconPickerComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .objectSetIconPickerComponent()
            .base(ObjectIconPickerBaseModule)
            .module(ObjectSetIconPickerModule)
            .build()
    }

    val objectLayoutComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectLayoutComponent()
            .module(ObjectLayoutModule)
            .build()
    }

    val objectAppearanceSettingComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectAppearanceSettingComponent()
            .module(ObjectAppearanceSettingModule)
            .build()
    }

    val objectAppearanceIconComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectAppearanceIconComponent()
            .module(ObjectAppearanceIconModule)
            .build()
    }

    val objectAppearancePreviewLayoutComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectAppearancePreviewLayoutComponent()
            .module(ObjectAppearancePreviewLayoutModule)
            .build()
    }

    val objectAppearanceCoverComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectAppearanceCoverComponent()
            .module(ObjectAppearanceCoverModule)
            .build()
    }

    val createBookmarkSubComponent = Component {
        main
            .createBookmarkBuilder()
            .createBookmarkModule(CreateBookmarkModule())
            .build()
    }

    val navigationComponent = Component {
        main.navigationComponentBuilder()
            .pageNavigationModule(PageNavigationModule)
            .build()
    }

    val otherSettingsComponent = Component {
        main.otherSettingsComponentBuilder()
            .module(OtherSettingsModule)
            .build()
    }

    val linkToObjectComponent = Component {
        main.linkToObjectBuilder()
            .module(LinkToObjectModule)
            .build()
    }

    val linkToObjectOrWebComponent = Component {
        main.linkToObjectOrWebBuilder()
            .module(LinkToObjectOrWebModule)
            .build()
    }

    val moveToComponent = Component {
        main
            .moveToBuilder()
            .module(MoveToModule)
            .build()
    }

    val objectSearchComponent = Component {
        main.objectSearchComponentBuilder()
            .module(ObjectSearchModule)
            .build()
    }

    val createSetComponent = Component {
        main.createSetComponentBuilder()
            .module(CreateSetModule)
            .build()
    }

    val createObjectTypeComponent = Component {
        main.createObjectTypeComponentBuilder()
            .module(CreateObjectTypeModule)
            .build()
    }

    val objectSetComponent = ComponentMap {
        main.objectSetComponentBuilder()
            .module(ObjectSetModule)
            .build()
    }

    val documentRelationComponent = DependentComponentMap { id ->
        editorComponent
            .get(id)
            .documentRelationSubComponent()
            .module(DocumentRelationModule)
            .build()
    }

    val viewerSortByComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerSortBySubComponent()
            .module(ViewerSortByModule)
            .build()
    }

    val relationTextValueDVComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .relationTextValueComponent()
            .module(RelationTextValueModule)
            .build()
    }

    val relationTextValueComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .relationTextValueComponent()
            .module(RelationTextValueModule)
            .build()
    }

    val objectSetObjectRelationDataValueComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .relationDateValueComponent()
            .module(RelationDateValueModule)
            .build()
    }

    val objectObjectRelationDateValueComponet = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .editRelationDateComponent()
            .module(RelationDateValueModule)
            .build()
    }

    val viewerFilterComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerFilterBySubComponent()
            .module(ViewerFilterModule)
            .build()
    }

    val viewerCustomizeComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerCustomizeSubComponent()
            .module(ViewerCustomizeModule)
            .build()
    }

    val objectSetRecordComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .objectSetRecordComponent()
            .module(ObjectSetRecordModule)
            .build()
    }

    val createDataViewViewerComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .createDataViewViewerSubComponent()
            .module(CreateDataViewViewerModule)
            .build()
    }

    val editDataViewViewerComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .editDataViewViewerComponent()
            .module(EditDataViewViewerModule)
            .build()
    }

    val objectSetObjectRelationValueComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .objectRelationValueComponent()
            .module(ObjectRelationValueModule)
            .build()
    }

    val addObjectSetObjectRelationValueComponent = DependentComponentMap { ctx ->
        objectSetObjectRelationValueComponent
            .get(ctx)
            .addObjectRelationValueComponent()
            .module(AddObjectRelationValueModule)
            .build()
    }

    val objectObjectRelationValueComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .editDocRelationComponent()
            .module(ObjectRelationValueModule)
            .build()
    }

    val addObjectObjectRelationValueComponent = DependentComponentMap { ctx ->
        objectObjectRelationValueComponent
            .get(ctx)
            .addObjectRelationValueComponent()
            .module(AddObjectRelationValueModule)
            .build()
    }

    val addObjectSetObjectRelationObjectValueComponent = DependentComponentMap { ctx ->
        objectSetObjectRelationValueComponent
            .get(ctx)
            .addObjectRelationObjectValueComponent()
            .module(AddObjectRelationModule)
            .build()
    }

    val addObjectRelationObjectValueComponent = DependentComponentMap { ctx ->
        objectObjectRelationValueComponent
            .get(ctx)
            .addObjectRelationObjectValueComponent()
            .module(AddObjectRelationModule)
            .build()
    }

    val relationFileValueComponent = DependentComponentMap { ctx ->
        objectObjectRelationValueComponent
            .get(ctx)
            .addRelationFileValueAddComponent()
            .module(AddFileRelationModule)
            .build()
    }

    val relationFileValueDVComponent = DependentComponentMap { ctx ->
        objectSetObjectRelationValueComponent
            .get(ctx)
            .addRelationFileValueAddComponent()
            .module(AddFileRelationModule)
            .build()
    }

    val manageViewerComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .manageViewerComponent()
            .module(ManageViewerModule)
            .build()
    }

    val viewerRelationsComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerRelationsComponent()
            .module(ViewerRelationsModule)
            .build()
    }

    val viewerCardSizeSelectComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerCardSizeSelectComponent()
            .module(ViewerCardSizeSelectModule)
            .build()
    }

    val viewerImagePreviewSelectComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerImagePreviewSelectComponent()
            .module(ViewerImagePreviewSelectModule)
            .build()
    }

    @Deprecated("Legacy")
    val dataviewViewerActionComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .dataviewViewerActionComponent()
            .module(DataViewViewerActionModule)
            .build()
    }

    val selectSortRelationComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .selectSortRelationComponent()
            .module(SelectSortRelationModule)
            .build()
    }

    val selectFilterRelationComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .selectFilterRelationComponent()
            .module(SelectFilterRelationModule)
            .build()
    }

    val createFilterComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .createFilterComponent()
            .module(CreateFilterModule)
            .build()
    }

    val pickFilterConditionComponentCreate = DependentComponentMap { ctx ->
        createFilterComponent
            .get(ctx)
            .createPickConditionComponent()
            .module(PickConditionModule)
            .build()
    }

    val pickFilterConditionComponentModify = DependentComponentMap { ctx ->
        modifyFilterComponent
            .get(ctx)
            .createPickConditionComponent()
            .module(PickConditionModule)
            .build()
    }

    val modifyFilterComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .modifyFilterComponent()
            .module(ModifyFilterModule)
            .build()
    }

    val viewerSortComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerSortComponent()
            .module(ViewerSortModule)
            .build()
    }

    val modifyViewerSortComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .modifyViewerSortComponent()
            .module(ModifyViewerSortModule)
            .build()
    }

    val objectCoverComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectCoverComponent()
            .module(SelectCoverObjectModule)
            .build()
    }

    val objectUnsplashComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectUnsplashComponent()
            .module(UnsplashModule)
            .build()
    }

    val objectSetUnsplashComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .objectUnsplashComponent()
            .module(UnsplashModule)
            .build()
    }

    val objectSetCoverComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .objectSetCoverComponent()
            .module(SelectCoverObjectSetModule)
            .build()
    }

    val objectMenuComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectMenuComponent()
            .base(ObjectMenuModuleBase)
            .module(ObjectMenuModule)
            .build()
    }

    val objectSetMenuComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .objectSetMenuComponent()
            .base(ObjectMenuModuleBase)
            .module(ObjectSetMenuModule)
            .build()
    }

    val relationAddToObjectComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .relationAddToObjectComponent()
            .module(RelationAddToObjectModule)
            .build()
    }

    val relationAddToDataViewComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .relationAddToDataViewComponent()
            .module(RelationAddToDataViewModule)
            .build()
    }

    val relationCreateFromScratchForObjectComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .relationCreateFromScratchForObjectComponent()
            .module(RelationCreateFromScratchForObjectModule)
            .build()
    }

    val relationFormatPickerObjectComponent = DependentComponentMap { ctx ->
        relationCreateFromScratchForObjectComponent
            .get(ctx)
            .relationFormatPickerComponent()
            .build()
    }

    val relationFormatPickerBlockComponent = DependentComponentMap { ctx ->
        relationCreateFromScratchForObjectBlockComponent
            .get(ctx)
            .relationFormatPickerComponent()
            .build()
    }

    val relationFormatPickerObjectSetComponent = DependentComponentMap { ctx ->
        relationCreateFromScratchForDataViewComponent
            .get(ctx)
            .relationFormatPickerComponent()
            .build()
    }

    val limitObjectTypeComponent = DependentComponentMap { ctx ->
        relationCreateFromScratchForObjectComponent.get(ctx)
            .limitObjectTypeComponent()
            .module(LimitObjectTypeModule)
            .build()
    }

    val limitObjectTypeBlockComponent = DependentComponentMap { ctx ->
        relationCreateFromScratchForObjectBlockComponent.get(ctx)
            .limitObjectTypeComponent()
            .module(LimitObjectTypeModule)
            .build()
    }

    val limitObjectTypeDataViewComponent = DependentComponentMap { ctx ->
        relationCreateFromScratchForDataViewComponent.get(ctx)
            .limitObjectTypeComponent()
            .module(LimitObjectTypeModule)
            .build()
    }

    val relationCreateFromScratchForObjectBlockComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .relationCreateFromScratchForObjectBlockComponent()
            .module(RelationCreateFromScratchForObjectBlockModule)
            .build()
    }

    val relationCreateFromScratchForDataViewComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .relationCreateFromScratchForDataViewComponent()
            .module(RelationCreateFromScratchForDataViewModule)
            .build()
    }

    val objectTypeChangeComponent = Component {
        main
            .objectTypeChangeComponent()
            .module(ObjectTypeChangeModule)
            .build()
    }

    // Settings

    val aboutAppComponent = Component {
        main.aboutAppComponent().module(AboutAppModule).build()
    }

    val accountAndDataComponent = Component {
        main.accountAndDataComponent().module(AccountAndDataModule).build()
    }

    val logoutWarningComponent = Component {
        main.logoutWarningComponent().module(LogoutWarningModule).build()
    }

    val mainSettingsComponent = Component {
        main.mainSettingsComponent().module(MainSettingsModule).build()
    }

    val appearanceComponent = Component {
        DaggerAppearanceComponent
            .factory()
            .create(findComponentDependencies())
    }

    class Component<T>(private val builder: () -> T) {

        private var instance: T? = null

        fun get() = instance ?: builder().also { instance = it }

        fun new() = builder().also { instance = it }

        fun release() {
            instance = null
        }
    }

    class ComponentMap<T>(private val builder: () -> T) {

        private val map = mutableMapOf<String, T>()

        fun get(id: String) = map[id] ?: builder().also { map[id] = it }

        fun new(id: String) = builder().also { map[id] = it }

        fun release(id: String) {
            map.remove(id)
        }
    }

    class DependentComponentMap<T>(private val builder: (Id) -> T) {

        private val map = mutableMapOf<String, T>()

        fun get(id: Id) = map[id] ?: builder(id).also { map[id] = it }

        fun new(id: Id) = builder(id).also { map[id] = it }

        fun release(id: Id) {
            map.remove(id)
        }
    }

    private inline fun <reified T : ComponentDependencies> findComponentDependencies(): T {
        return provider.dependencies[T::class.java] as T
    }
}