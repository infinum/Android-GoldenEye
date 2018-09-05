package co.infinum.goldeneye.extensions

fun Int.limit(bottomLimit: Int, upperLimit: Int) = Math.min(Math.max(this, bottomLimit), upperLimit)
fun Double.limit(bottomLimit: Double, upperLimit: Double) = Math.min(Math.max(this, bottomLimit), upperLimit)
fun Float.limit(bottomLimit: Float, upperLimit: Float) = Math.min(Math.max(this, bottomLimit), upperLimit)