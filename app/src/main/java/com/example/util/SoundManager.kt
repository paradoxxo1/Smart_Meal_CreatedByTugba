package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

object SoundManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isMuted = false

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    // --- STANDARD SOUNDS ---
    // Generate Success Chime (Harmonic major arpeggio with warm bell decay)
    private val successPcm: ShortArray by lazy {
        val sampleRate = 44100
        val durationSec = 0.75
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)

        val notes = listOf(
            Pair(0.00, 523.25), // C5
            Pair(0.12, 659.25), // E5
            Pair(0.24, 783.99), // G5
            Pair(0.36, 1046.50) // C6
        )

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            var sampleVal = 0.0

            for ((startT, freq) in notes) {
                if (t >= startT) {
                    val noteT = t - startT
                    val envelope = exp(-noteT * 5.5)
                    val wave = sin(2.0 * PI * freq * noteT) +
                            0.35 * sin(2.0 * PI * (freq * 2) * noteT) +
                            0.15 * sin(2.0 * PI * (freq * 3) * noteT)
                    sampleVal += wave * envelope * 0.45
                }
            }

            val masterEnvelope = (1.0 - exp(-t * 30.0)) * (1.0 - t / durationSec).coerceIn(0.0, 1.0)
            sampleVal *= masterEnvelope
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        samples
    }

    // Generate Error / Wrong Buzzer (Low punchy double buzz tone)
    private val errorPcm: ShortArray by lazy {
        val sampleRate = 44100
        val durationSec = 0.42
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val inFirstPulse = t in 0.0..0.16
            val inSecondPulse = t in 0.20..0.38

            var sampleVal = 0.0
            if (inFirstPulse || inSecondPulse) {
                val pulseT = if (inFirstPulse) t else (t - 0.20)
                val freq = 185.0 - pulseT * 80.0
                val fundamental = sin(2.0 * PI * freq * pulseT)
                val h3 = sin(2.0 * PI * (freq * 3) * pulseT) * 0.35
                val h5 = sin(2.0 * PI * (freq * 5) * pulseT) * 0.15
                val env = exp(-pulseT * 8.5)
                sampleVal = (fundamental + h3 + h5) * env * 0.65
            }

            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        samples
    }

    // Generate Tap Click for responsive keypad feedback
    private val tapPcm: ShortArray by lazy {
        val sampleRate = 44100
        val durationSec = 0.03
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 900.0 - t * 15000.0
            val env = exp(-t * 140.0)
            val sampleVal = sin(2.0 * PI * freq.coerceAtLeast(100.0) * t) * env * 0.4
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        samples
    }

    // --- ZOMBIE MODE SOUNDS (Iconic Minecraft Authentic Zombie Style) ---
    // Authentic Minecraft Zombie "Uuuuuurgh... Brrrr" (Vowel shifting formants: 'oo' -> 'err' -> 'gh')
    private val zombieGroanPcm: ShortArray by lazy {
        val sampleRate = 44100
        val durationSec = 1.25
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        val rnd = Random(555)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / durationSec

            // Minecraft zombie fundamental pitch: starts ~90Hz, dips down to 65Hz, slow undulating pitch wobble
            val pitch = 92.0 - (progress * 28.0) + sin(2.0 * PI * 5.0 * t) * 6.0

            // Multi-harmonic glottal pulse (raspy saw-like buzz)
            val glottalSaw = (sin(2.0 * PI * pitch * t) +
                              0.85 * sin(2.0 * PI * pitch * 2.0 * t) +
                              0.65 * sin(2.0 * PI * pitch * 3.0 * t) +
                              0.45 * sin(2.0 * PI * pitch * 4.0 * t) +
                              0.30 * sin(2.0 * PI * pitch * 5.0 * t) +
                              0.20 * sin(2.0 * PI * pitch * 6.0 * t))

            // Dynamic Vowel Formant Transition:
            // Part 1 (0.0 to 0.4s): "UUU" formant (F1: 300Hz, F2: 800Hz)
            // Part 2 (0.4 to 0.9s): "ERRR" formant (F1: 500Hz, F2: 1350Hz)
            // Part 3 (0.9 to 1.25s): "GHHH" formant (F1: 250Hz, F2: 700Hz + vocal fry rasp)
            val f1Freq = when {
                progress < 0.35 -> 300.0 + progress * 200.0
                progress < 0.75 -> 500.0 - (progress - 0.35) * 200.0
                else -> 300.0 - (progress - 0.75) * 150.0
            }
            val f2Freq = when {
                progress < 0.35 -> 800.0 + (progress / 0.35) * 550.0
                progress < 0.75 -> 1350.0 - ((progress - 0.35) / 0.40) * 600.0
                else -> 750.0
            }

            val formant1 = sin(2.0 * PI * f1Freq * t) * 0.7
            val formant2 = sin(2.0 * PI * f2Freq * t) * 0.4

            // Authentic low-frequency raspy chest growl & gargle
            val throatGargle = (rnd.nextDouble() * 2.0 - 1.0) * 0.25 * (0.8 + 0.4 * sin(2.0 * PI * 24.0 * t))

            // Tremolo wobble (classic MC monster vibration at 11Hz)
            val tremolo = 0.75 + 0.25 * sin(2.0 * PI * 11.5 * t)

            // Attack & Decay Envelope
            val attack = (1.0 - exp(-t * 12.0))
            val decay = exp(-progress * 2.8)
            val envelope = attack * decay

            val sampleVal = ((glottalSaw * 0.4 + formant1 + formant2) + throatGargle) * tremolo * envelope * 0.8
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        samples
    }

    // Zombie Kill / Slash & Monster Defeat ("Splat/Slash" + Satisfying Exp Orb Chime)
    private val zombieApplausePcm: ShortArray by lazy {
        val sampleRate = 44100
        val durationSec = 0.95
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        val rnd = Random(888)

        // Minecraft XP Orb / Level Up Ding frequencies (E6, G#6, B6, E7)
        val xpNotes = listOf(
            Pair(0.12, 1318.51), // E6
            Pair(0.24, 1661.22), // G#6
            Pair(0.36, 1975.53), // B6
            Pair(0.48, 2637.02)  // E7 (Bright ding!)
        )

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            var sampleVal = 0.0

            // 1. Sword Hit / Zombie Death Slash & Poof Puff
            if (t < 0.18) {
                val hitEnv = exp(-t * 45.0)
                val swordNoise = (rnd.nextDouble() * 2.0 - 1.0) * 0.65
                val bladeTone = sin(2.0 * PI * (950.0 - t * 4000.0) * t) * 0.45
                sampleVal += (swordNoise + bladeTone) * hitEnv * 0.8
            }

            // 2. Zombie Poof / Death Puff sound
            if (t in 0.05..0.35) {
                val poofT = t - 0.05
                val poofNoise = (rnd.nextDouble() * 2.0 - 1.0) * exp(-poofT * 18.0) * 0.35
                sampleVal += poofNoise
            }

            // 3. Iconic Sparkle / XP Orb Sound
            for ((startT, freq) in xpNotes) {
                if (t >= startT) {
                    val dt = t - startT
                    val env = exp(-dt * 14.0)
                    val tone = sin(2.0 * PI * freq * dt) + 0.15 * sin(2.0 * PI * (freq * 2.0) * dt)
                    sampleVal += tone * env * 0.40
                }
            }

            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        samples
    }

    // --- BOMB DEFUSAL SOUNDS ---
    // Bomb Ticking Sound (Crisp metallic / tension countdown clock pulse)
    private val bombTickNormalPcm: ShortArray by lazy {
        val sampleRate = 44100
        val durationSec = 0.045
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 1250.0 - (t * 8000.0)
            val env = exp(-t * 110.0)
            val sampleVal = (sin(2.0 * PI * freq * t) + 0.3 * sin(2.0 * PI * (freq * 2.5) * t)) * env * 0.65
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        samples
    }

    // Critical Bomb Ticking Sound (High pitch urgent double-chirp for final countdown)
    private val bombTickCriticalPcm: ShortArray by lazy {
        val sampleRate = 44100
        val durationSec = 0.065
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val isSecondClick = t >= 0.030
            val clickT = if (isSecondClick) t - 0.030 else t
            val freq = if (isSecondClick) 2800.0 else 2400.0
            val env = exp(-clickT * 140.0)
            val sampleVal = sin(2.0 * PI * freq * clickT) * env * 0.8
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        samples
    }

    // Bomb Defused (Realistic wire snip click + high-tech power-down sigh + clear disarm chime)
    private val bombDefusedPcm: ShortArray by lazy {
        val sampleRate = 44100
        val durationSec = 0.85
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        val rnd = Random(888)

        val disarmTones = listOf(
            Pair(0.12, 1046.50), // C6
            Pair(0.24, 1318.51), // E6
            Pair(0.36, 1567.98), // G6
            Pair(0.48, 2093.00)  // C7
        )

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            var sampleVal = 0.0

            // 1. Mechanical wire cutter "Snip-Click"
            if (t < 0.08) {
                val snipEnv = exp(-t * 90.0)
                val shearNoise = (rnd.nextDouble() * 2.0 - 1.0) * 0.6
                val clickTone = sin(2.0 * PI * 3200.0 * t) * 0.4
                sampleVal += (shearNoise + clickTone) * snipEnv * 0.9
            }

            // 2. High-tech disarmed digital chime sequence
            for ((startT, freq) in disarmTones) {
                if (t >= startT) {
                    val dt = t - startT
                    val env = exp(-dt * 9.0)
                    val wave = sin(2.0 * PI * freq * dt) + 0.15 * sin(2.0 * PI * (freq * 2.0) * dt)
                    sampleVal += wave * env * 0.4
                }
            }

            // 3. Electrical power-down sweep
            if (t in 0.08..0.70) {
                val powerT = t - 0.08
                val sweepFreq = 650.0 * exp(-powerT * 4.5)
                val powerEnv = exp(-powerT * 3.5) * (1.0 - exp(-powerT * 20.0))
                sampleVal += sin(2.0 * PI * sweepFreq * powerT) * powerEnv * 0.25
            }

            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        samples
    }

    // Heavy Realistic Bomb Explosion (Supersonic crack, deep shockwave bass rumble & decaying fireball)
    private val bombExplosionPcm: ShortArray by lazy {
        val sampleRate = 44100
        val durationSec = 1.20
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        val rnd = Random(101)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate

            // 1. Initial shockwave crack (0 - 40ms)
            val crack = if (t < 0.04) {
                val crackT = t
                ((rnd.nextDouble() * 2.0 - 1.0) + sin(2.0 * PI * 450.0 * crackT)) * exp(-crackT * 80.0) * 0.9
            } else 0.0

            // 2. Deep Sub-bass pressure wave (120Hz -> 35Hz drop)
            val subFreq = (110.0 * exp(-t * 2.5)).coerceAtLeast(30.0)
            val subWave = (sin(2.0 * PI * subFreq * t) + 0.4 * sin(2.0 * PI * (subFreq * 0.5) * t)) * 
                          exp(-t * 2.8) * (1.0 - exp(-t * 60.0)) * 0.8

            // 3. Fireball & debris turbulent noise
            val noise = (rnd.nextDouble() * 2.0 - 1.0) * exp(-t * 3.2) * 0.55

            val sampleVal = crack + subWave + noise
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        samples
    }

    // --- SPACE MISSION SOUNDS ---
    // Rocket Thruster Boost (Ascending frequency whooshing roar)
    private val rocketBoostPcm: ShortArray by lazy {
        val sampleRate = 44100
        val durationSec = 0.80
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        val rnd = Random(555)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 120.0 + (t * 450.0)
            val wave = sin(2.0 * PI * freq * t) * 0.4
            val thrusterNoise = (rnd.nextDouble() * 2.0 - 1.0) * 0.45
            val env = (1.0 - t / durationSec) * (1.0 - exp(-t * 20.0))
            val sampleVal = (wave + thrusterNoise) * env * 0.7
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        samples
    }

    // Space Warning (Sci-fi dual klaxon alarm)
    private val spaceAlarmPcm: ShortArray by lazy {
        val sampleRate = 44100
        val durationSec = 0.45
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val isFirstPulse = t < 0.20
            val pulseT = if (isFirstPulse) t else (t - 0.22)
            val freq = if (isFirstPulse) 950.0 else 720.0
            val wave = sin(2.0 * PI * freq * pulseT)
            val env = exp(-pulseT * 8.0)
            val sampleVal = wave * env * 0.55
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        samples
    }

    // Playback Triggers
    fun playSuccess() {
        if (isMuted) return
        scope.launch { playPcmAudio(successPcm, 44100) }
    }

    fun playError() {
        if (isMuted) return
        scope.launch { playPcmAudio(errorPcm, 44100) }
    }

    fun playTap() {
        if (isMuted) return
        scope.launch { playPcmAudio(tapPcm, 44100) }
    }

    fun playZombieApplause() {
        if (isMuted) return
        scope.launch { playPcmAudio(zombieApplausePcm, 44100) }
    }

    fun playZombieGroan() {
        if (isMuted) return
        scope.launch { playPcmAudio(zombieGroanPcm, 44100) }
    }

    fun playBombDefused() {
        if (isMuted) return
        scope.launch { playPcmAudio(bombDefusedPcm, 44100) }
    }

    fun playBombExplosion() {
        if (isMuted) return
        scope.launch { playPcmAudio(bombExplosionPcm, 44100) }
    }

    fun playBombTick(isCritical: Boolean = false) {
        if (isMuted) return
        scope.launch {
            if (isCritical) {
                playPcmAudio(bombTickCriticalPcm, 44100)
            } else {
                playPcmAudio(bombTickNormalPcm, 44100)
            }
        }
    }

    fun playRocketBoost() {
        if (isMuted) return
        scope.launch { playPcmAudio(rocketBoostPcm, 44100) }
    }

    fun playSpaceAlarm() {
        if (isMuted) return
        scope.launch { playPcmAudio(spaceAlarmPcm, 44100) }
    }

    private fun playPcmAudio(pcmData: ShortArray, sampleRate: Int) {
        try {
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(pcmData.size * 2)

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(pcmData, 0, pcmData.size)
            audioTrack.play()

            Thread.sleep((pcmData.size.toDouble() / sampleRate * 1000).toLong() + 50)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio device exceptions safely
        }
    }

    fun vibrateError(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 100, 60, 140)
                    val amplitudes = intArrayOf(0, 255, 0, 220)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 100, 60, 140), -1)
                }
            }
        } catch (_: Exception) {
            // Ignore if vibration unavailable
        }
    }
}
