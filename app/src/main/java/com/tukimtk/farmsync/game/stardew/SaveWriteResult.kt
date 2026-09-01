package com.tukimtk.farmsync.game.stardew

sealed class SaveWriteResult {
    object SuccessVerified : SaveWriteResult()
    object BackupFailed : SaveWriteResult()
    object ShizukuNotReady : SaveWriteResult()
    object InvalidDestination : SaveWriteResult()
    object MainSaveStageFailed : SaveWriteResult()
    object SaveGameInfoStageFailed : SaveWriteResult()
    object MainSaveReplaceFailed : SaveWriteResult()
    object SaveGameInfoReplaceFailed : SaveWriteResult()
    object PermissionDenied : SaveWriteResult()
    object VerificationFailed : SaveWriteResult()
    object ReloadFailed : SaveWriteResult()
    object RollbackFailed : SaveWriteResult()
    object UnexpectedFailure : SaveWriteResult()
}
