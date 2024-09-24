package com.application.svcafe

import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val imageView: ImageView = findViewById(R.id.imageView)

        // Load animations
        val zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        val zoomOut = AnimationUtils.loadAnimation(this, R.anim.zoom_out)

        // Start zoom-in animation
        imageView.startAnimation(zoomIn)

        // Set animation listeners
        zoomIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                imageView.alpha = 1f
            }

            override fun onAnimationEnd(animation: Animation) {
                // Start zoom-out animation after zoom-in ends
                imageView.startAnimation(zoomOut)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        zoomOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // Navigate to IntroActivity after zoom-out animation ends
                val intent = Intent(this@SplashScreenActivity, IntroActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

                // Set enter and exit transitions
                window.enterTransition = Fade().apply {
                    duration = 400
                    interpolator = AccelerateInterpolator()
                }
                window.exitTransition = Fade().apply {
                    duration = 400
                    interpolator = AccelerateInterpolator()
                }

                finish()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }
}
