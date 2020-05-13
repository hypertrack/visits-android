package com.hypertrack.android.view_models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.utils.Destination
import com.hypertrack.android.utils.getServiceLocator
import io.branch.referral.Branch
import io.branch.referral.BranchError
import kotlinx.coroutines.launch
import org.json.JSONObject

class SplashScreenViewModel (
    application: Application
) : AndroidViewModel(application), Branch.BranchReferralInitListener  {

    private val driverRepository = application.getServiceLocator().getDriverRepo()
    private val accountRepository = application.getServiceLocator().getAccountRepo()

    private val _showSpinner = MutableLiveData<Boolean>(true)
    private val _noAccountFragment = MutableLiveData<Boolean>(false)
    private val _destination = MutableLiveData<Destination>(
        Destination.SPLASH_SCREEN)

    /** Show a loading spinner if true */
    val spinner: LiveData<Boolean>
        get() = _showSpinner

    /** Show no-account error fragment if true */
    val noAccountFragment: LiveData<Boolean>
        get() = _noAccountFragment

    val destination: LiveData<Destination>
        get() = _destination

    fun login() {
        when {
            driverRepository.hasDriverId -> {
                // already logged in
                _showSpinner.postValue(false)
                _destination.postValue(Destination.LIST_VIEW)
            }
            accountRepository.isVerifiedAccount -> {
                // publishable key already verified
                _showSpinner.postValue(false)
                _destination.postValue(Destination.LOGIN)
            }
            else -> {
                Log.d(TAG, "No publishable key found, waiting for Branch IO")
            }
        }
    }

    override fun onInitFinished(referringParams: JSONObject?, error: BranchError?) {
        Log.d(TAG, "Branch init finished with params $referringParams")
        val key = referringParams?.optString("publishable_key")!!
        if (key.isNotEmpty()) {
            Log.d(TAG, "Got key $key")
                try {
                    viewModelScope.launch {
                        val correctKey = accountRepository.onKeyReceived(key, this@SplashScreenViewModel.getApplication())
                        Log.d(TAG, "onKeyReceived finished")
                        if (correctKey) {
                            Log.d(TAG, "Key validated successfully")

                            _showSpinner.postValue(false)
                            _destination.postValue(Destination.LOGIN)
                        } else {
                            noPkHanlder()
                        }
                    }
                    Log.d(TAG, "coroutine finished")
                    return
                } catch (e : Throwable) {
                    Log.w(TAG, "Cannot validate the key", e)
                }
        } else {
            error?.let { Log.e(TAG, "Branch IO init failed. $error") }
            noPkHanlder()

        }


    }

    private fun noPkHanlder() {
        Log.e(TAG, "No publishable key")
        _showSpinner.postValue(false)
        _noAccountFragment.postValue(true)
    }

    companion object {
        const val TAG = "SplashScreenVM"
    }

}
