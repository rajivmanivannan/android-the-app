package com.ronetech.theapp.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ronetech.theapp.databinding.ActivityLoginBinding
import com.ronetech.theapp.ui.home.HomeActivity
import com.ronetech.theapp.ui.signup.SignUpActivity

/**
 * LoginActivity
 */
class LoginActivity : AppCompatActivity() {

  private lateinit var loginViewModel: LoginViewModel
  private lateinit var binding: ActivityLoginBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val usernameTextInputLayout = binding.usernameTextInputLayout
    val username = binding.username
    val passwordTextInputLayout = binding.passwordTextInputLayout
    val password = binding.password
    val login = binding.login
    val loading = binding.loading
    val signUp = binding.signUp

    loginViewModel = ViewModelProvider(this, LoginViewModelFactory())[LoginViewModel::class.java]

    loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
      val loginState = it ?: return@Observer

      // disable login button unless both username / password is valid
      login.isEnabled = loginState.isDataValid

      if (loginState.usernameError != null) {
        usernameTextInputLayout.error = getString(loginState.usernameError)
      }else{
        usernameTextInputLayout.error = null
      }
      if (loginState.passwordError != null) {
        passwordTextInputLayout.error = getString(loginState.passwordError)
      }else{
        passwordTextInputLayout.error = null
      }
    })

    loginViewModel.loginResult.observe(this@LoginActivity, Observer {
      val loginResult = it ?: return@Observer

      loading.visibility = View.GONE
      if (loginResult.error != null) {
        showLoginFailed(loginResult.error)
      }
      if (loginResult.success != null) {
        updateUiWithUser(loginResult.success)
      }
      setResult(Activity.RESULT_OK)

      //Complete and destroy login activity once successful
      finish()
    })

    username.afterTextChanged {
      loginViewModel.loginDataChanged(
        username.text.toString(),
        password.text.toString()
      )
    }

    password.apply {
      afterTextChanged {
        loginViewModel.loginDataChanged(
          username.text.toString(),
          password.text.toString()
        )
      }

      setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
          EditorInfo.IME_ACTION_DONE ->
            loginViewModel.login(
              username.text.toString(),
              password.text.toString()
            )
        }
        false
      }

      login.setOnClickListener {
        loading.visibility = View.VISIBLE
        loginViewModel.login(username.text.toString(), password.text.toString())
      }
    }

    signUp.setOnClickListener {
      startActivity(Intent(this, SignUpActivity::class.java))
    }
  }

  private fun updateUiWithUser(model: LoggedInUserView) {
    startActivity(Intent(this, HomeActivity::class.java))
  }


  private fun showLoginFailed(@StringRes errorString: Int) {
    Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT)
      .show()
  }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
  this.addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(editable: Editable?) {
      afterTextChanged.invoke(editable.toString())
    }

    override fun beforeTextChanged(
      s: CharSequence,
      start: Int,
      count: Int,
      after: Int
    ) {
    }

    override fun onTextChanged(
      s: CharSequence,
      start: Int,
      before: Int,
      count: Int
    ) {
    }
  })
}