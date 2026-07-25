package com.chris.chipherlink.ui.profile

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chris.chipherlink.CipherLinkApplication
import com.chris.chipherlink.data.local.SecurePreferences
import com.chris.chipherlink.data.local.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

data class ProfileUiState(
    val profile: UserProfileEntity? = null,
    val isLoading: Boolean = true,
    val displayName: String = "",
    val isEditing: Boolean = false,
    val photoUri: Uri? = null,
    val pendingCropUri: Uri? = null,
    val isCropping: Boolean = false,
    val cipherLinkId: String = ""
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CipherLinkApplication
    private val profileRepo = app.profileRepository
    private val securePrefs = SecurePreferences(application)

    private val userId: String = app.authRepository.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Load CipherLink ID from user entity
            val currentUser = app.database.userDao().getById(userId)
            _uiState.value = _uiState.value.copy(
                cipherLinkId = currentUser?.cipherLinkId ?: "N/A"
            )

            profileRepo.observeProfile(userId).collect { profile ->
                _uiState.value = _uiState.value.copy(
                    profile = profile,
                    isLoading = false,
                    displayName = profile?.displayName ?: "",
                    photoUri = securePrefs.profilePhotoUriValue?.let { Uri.parse(it) }
                )
            }
        }
    }

    fun startEditing() {
        _uiState.value = _uiState.value.copy(isEditing = true)
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(
            isEditing = false,
            displayName = _uiState.value.profile?.displayName ?: ""
        )
    }

    fun updateDisplayName(name: String) {
        _uiState.value = _uiState.value.copy(displayName = name)
    }

    fun saveProfile() {
        val name = _uiState.value.displayName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            profileRepo.updateDisplayName(userId, name)
            _uiState.value = _uiState.value.copy(isEditing = false)
        }
    }

    fun onPhotoSelected(uri: Uri?) {
        if (uri == null) return
        _uiState.value = _uiState.value.copy(pendingCropUri = uri, isCropping = true)
    }

    fun confirmCrop() {
        val uri = _uiState.value.pendingCropUri ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCropping = true)
            val savedPath = withContext(Dispatchers.IO) {
                processAndSavePhoto(uri)
            }
            if (savedPath != null) {
                profileRepo.updatePhotoPath(userId, savedPath)
                securePrefs.profilePhotoUriValue = savedPath
                _uiState.value = _uiState.value.copy(
                    photoUri = Uri.parse(savedPath),
                    pendingCropUri = null,
                    isCropping = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    pendingCropUri = null,
                    isCropping = false
                )
            }
        }
    }

    fun cancelCrop() {
        _uiState.value = _uiState.value.copy(
            pendingCropUri = null,
            isCropping = false
        )
    }

    private fun processAndSavePhoto(uri: Uri): String? {
        return try {
            val context = getApplication<Application>()

            // 1. Decode the original bitmap
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // 2. Handle EXIF rotation
            val rotatedBitmap = fixRotation(originalBitmap, uri)

            // 3. Crop to square (center crop)
            val size = minOf(rotatedBitmap.width, rotatedBitmap.height)
            val x = (rotatedBitmap.width - size) / 2
            val y = (rotatedBitmap.height - size) / 2
            val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, x, y, size, size)

            // 4. Resize to 512x512
            val resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, TARGET_SIZE, TARGET_SIZE, true)

            // 5. Save as WebP
            val fileName = "profile_photo_${userId}.webp"
            val file = File(context.filesDir, fileName)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 85, outputStream)
            file.writeBytes(outputStream.toByteArray())

            // 6. Cleanup
            originalBitmap.recycle()
            if (rotatedBitmap !== originalBitmap) rotatedBitmap.recycle()
            croppedBitmap.recycle()
            resizedBitmap.recycle()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun fixRotation(bitmap: Bitmap, uri: Uri): Bitmap {
        return try {
            val context = getApplication<Application>()
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            val rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> return bitmap
            }

            val matrix = Matrix().apply { postRotate(rotation) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            rotated
        } catch (e: Exception) {
            bitmap
        }
    }

    fun getCipherLinkId(): String {
        return _uiState.value.cipherLinkId
    }

    fun getIdentityId(): String {
        return _uiState.value.cipherLinkId
    }

    fun formatCreationDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    companion object {
        private const val TARGET_SIZE = 512
    }
}
