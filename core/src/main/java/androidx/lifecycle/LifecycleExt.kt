package androidx.lifecycle


private const val DEFAULT_KEY = "androidx.lifecycle.ViewModelProvider.DefaultKey"
private const val CUSTOM_KEY = "android.core.logistic.CustomKey"

fun ViewModel.isShared(owner: ViewModelStoreOwner): Boolean {
    val modelClass = javaClass
    return owner.viewModelStore.get(DEFAULT_KEY + ":" + modelClass.canonicalName) != null
}

@Suppress("unchecked_cast")
internal fun <T : ViewModel> ViewModelStore.getOrCreate(key: String, function: () -> T): T {
    val customKey = "$CUSTOM_KEY:$key"
    var viewModel = get(customKey) as? T

    if (viewModel == null) {
        viewModel = function()
        put(customKey, viewModel)
    }
    return viewModel
}

internal val ViewModelStore.keys get() = keys()

internal fun ViewModelStore._get(key: String) = get(key)

internal fun <T> ViewModel._setTagIfAbsent(key: String, newValue: T) {
    setTagIfAbsent(key, newValue)
}

internal fun <T> ViewModel._getTag(key: String): T {
    return getTag(key)
}