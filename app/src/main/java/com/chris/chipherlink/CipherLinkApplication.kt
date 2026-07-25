package com.chris.chipherlink

import android.app.Application
import com.chris.chipherlink.ai.AiPrivacyManager
import com.chris.chipherlink.ai.AiProviderManager
import com.chris.chipherlink.backup.BackupManager
import com.chris.chipherlink.data.local.AppDatabase
import com.chris.chipherlink.data.local.SecurePreferences
import com.chris.chipherlink.data.local.SessionManager
import com.chris.chipherlink.data.repository.AiRepository
import com.chris.chipherlink.data.repository.AuthRepository
import com.chris.chipherlink.data.repository.ChatRepository
import com.chris.chipherlink.data.repository.ContactRepository
import com.chris.chipherlink.data.repository.ProfileRepository
import com.chris.chipherlink.integrity.IdentityManager
import com.chris.chipherlink.integrity.IntegrityManager
import com.chris.chipherlink.recovery.RecoveryManager
import com.chris.chipherlink.security.KeyManager
import com.chris.chipherlink.security.SecureStorage
import com.chris.chipherlink.security.VaultManager
import com.chris.chipherlink.security.VersionManager
import com.chris.chipherlink.update.UpdateChecker

class CipherLinkApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val sessionManager: SessionManager by lazy { SessionManager(this) }
    val securePreferences: SecurePreferences by lazy { SecurePreferences(this) }

    // Security
    val identityManager: IdentityManager by lazy { IdentityManager(this) }
    val integrityManager: IntegrityManager by lazy { IntegrityManager(this, identityManager) }
    val keyManager: KeyManager by lazy { KeyManager(this) }
    val secureStorage: SecureStorage by lazy { SecureStorage(this) }
    val vaultManager: VaultManager by lazy {
        VaultManager(this, identityManager, integrityManager, keyManager, secureStorage)
    }

    // Backup & Recovery
    val backupManager: BackupManager by lazy { BackupManager(this) }
    val recoveryManager: RecoveryManager by lazy {
        RecoveryManager(this, identityManager, securePreferences)
    }

    // AI
    val aiProviderManager: AiProviderManager by lazy { AiProviderManager() }
    val aiPrivacyManager: AiPrivacyManager by lazy { AiPrivacyManager(this) }

    // Repositories
    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(database.userProfileDao())
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            userDao = database.userDao(),
            sessionDao = database.sessionDao(),
            sessionManager = sessionManager,
            identityManager = identityManager,
            integrityManager = integrityManager,
            profileRepository = profileRepository
        )
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(
            chatDao = database.chatDao(),
            messageDao = database.messageDao(),
            integrityManager = integrityManager
        )
    }

    val aiRepository: AiRepository by lazy {
        AiRepository(
            aiChatDao = database.aiChatDao(),
            aiMessageDao = database.aiMessageDao()
        )
    }

    val contactRepository: ContactRepository by lazy {
        ContactRepository(
            contactDao = database.contactDao(),
            userDao = database.userDao()
        )
    }

    val updateChecker: UpdateChecker by lazy { UpdateChecker(this) }
    val versionManager: VersionManager by lazy { VersionManager(this) }
}
