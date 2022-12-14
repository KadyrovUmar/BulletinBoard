package com.example.bulletinboard.account

import android.util.Log
import com.example.bulletinboard.R
import android.widget.Toast
import com.example.bulletinboard.MainActivity
import com.example.bulletinboard.constants.FirebaseAuthConstants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*

class Account(act: MainActivity) {
    private val act = act
    private lateinit var signInClient: GoogleSignInClient

    fun signUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    act.mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                singUnWithEmailSuccessful(task.result.user!!)
                            } else {
                                signUpWithEmailException(task.exception!!, email, password)
                            }
                        }
                }
            }
        }
    }

    private fun singUnWithEmailSuccessful(user: FirebaseUser) {
        sendEmailVerification(user)
        act.uiUpdate(user)
    }

    private fun signUpWithEmailException(e: Exception, email: String, password: String) {
        if (e is FirebaseAuthUserCollisionException) {
            val exception = e as FirebaseAuthUserCollisionException
            if (exception.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                linkEmailToG(email, password)
            }
        } else if (e is FirebaseAuthInvalidCredentialsException) {
            val exception = e as FirebaseAuthInvalidCredentialsException
            if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG)
                    .show()
            }
        }
        if (e is FirebaseAuthWeakPasswordException) {
            //Log.d("MyLog","Exception : ${e.errorCode}")
            if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_LONG)
                    .show()
            }
        }
        //FirebaseAuthWeakPasswordException
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    act.mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                act.uiUpdate(task.result?.user)
                            } else {
                                signInWithEmailException(task.exception!!, email, password)
                            }
                        }
                }
            }
        }
    }

    private fun signInWithEmailException(e: Exception, email: String, password: String) {
        //Log.d("MyLog", "Exception : ${e}")
        if (e is FirebaseAuthInvalidCredentialsException) {
            //Log.d("MyLog","Exception : ${task.exception}")
            val exception = e as FirebaseAuthInvalidCredentialsException
            // Log.d("MyLog","Exception 2 : ${exception.errorCode}")
            if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(
                    act,
                    FirebaseAuthConstants.ERROR_INVALID_EMAIL,
                    Toast.LENGTH_LONG
                ).show()
            } else if (exception.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                Toast.makeText(
                    act,
                    FirebaseAuthConstants.ERROR_WRONG_PASSWORD,
                    Toast.LENGTH_LONG
                ).show()
            }
        } else if (e is FirebaseAuthInvalidUserException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                Toast.makeText(
                    act,
                    FirebaseAuthConstants.ERROR_USER_NOT_FOUND,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun linkEmailToG(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (act.mAuth.currentUser != null) {
            act.mAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        act,
                        act.resources.getString(R.string.link_done),
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        } else {
            Toast.makeText(act, act.resources.getString(R.string.enter_to_g), Toast.LENGTH_LONG)
                .show()
        }
    }

    // register google
    private fun getSignInClient(): GoogleSignInClient {
        // ???????????????? ???????????????????? string
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(act.getString(R.string.default_web_client_id)).requestEmail().build()
        return GoogleSignIn.getClient(act, gso)
    }

    fun signInWithGoogle() {
        signInClient = getSignInClient()
        val intent = signInClient.signInIntent
        // ???????? ???????????????????? ???? activity
        act.googleSignInLauncher.launch(intent)
    }

    fun signOutG() {
        getSignInClient().signOut()
    }

    fun signInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(act, "Host deleted", Toast.LENGTH_LONG).show()
                act.mAuth.signInWithCredential(credential).addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        Toast.makeText(act, "Sign in done", Toast.LENGTH_LONG).show()
                        act.uiUpdate(task2.result?.user)
                    } else {
                        Toast.makeText(
                            act, "Google Sign In Exception : ${task2.exception}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d("MyLog", "Google Sign In Exception : ${task2.exception}")
                    }
                }
            } else {
                Toast.makeText(act, "Host not deleted", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.send_verification_done),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.send_verification_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun signInAnonymously(listener: Listener) {
        act.mAuth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                listener.onComplete()
                Toast.makeText(act, "???? ?????????? ?????? ??????????", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(act, "???? ?????????????? ?????????? ?????? ??????????", Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface Listener {
        fun onComplete()
    }
}