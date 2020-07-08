@file:Suppress("NOTHING_TO_INLINE")

package org.kiot.util

import kotlinx.io.core.String
import kotlinx.io.core.toByteArray
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

interface Binarizable

interface Binarizer<T> {
	fun binarize(bin: Binary, value: T)
	fun debinarize(bin: Binary): T
	fun measure(value: T): Int
}

interface StaticBinarizer<T> : Binarizer<T> {
	val binarySize: Int
	override fun measure(value: T) = binarySize
}

inline fun <T> Binarizer<T>.binarize(value: T, array: ByteArray, startIndex: Int = 0, endIndex: Int = array.size) {
	binarize(array.asBinary(startIndex, endIndex), value)
}

val Boolean.Companion.binarySize: Int
	inline get() = 1
val Char.Companion.binarySize: Int
	inline get() = 2
val Byte.Companion.binarySize: Int
	inline get() = 1
val Short.Companion.binarySize: Int
	inline get() = 2
val Int.Companion.binarySize: Int
	inline get() = 4
val Long.Companion.binarySize: Int
	inline get() = 8
val Float.Companion.binarySize: Int
	inline get() = 4
val Double.Companion.binarySize: Int
	inline get() = 8

class DummyField<R, T>(private val value: T) : ReadOnlyProperty<R, T> {
	override fun getValue(thisRef: R, property: KProperty<*>) = value
}

val Boolean.Companion.binarizer: StaticBinarizer<Boolean> by DummyField(object : StaticBinarizer<Boolean> {
	override fun binarize(bin: Binary, value: Boolean) = bin.put(value)
	override fun debinarize(bin: Binary) = bin.boolean()
	override val binarySize: Int
		get() = Boolean.binarySize
})
val Char.Companion.binarizer: StaticBinarizer<Char> by DummyField(object : StaticBinarizer<Char> {
	override fun binarize(bin: Binary, value: Char) = bin.put(value)
	override fun debinarize(bin: Binary) = bin.char()
	override val binarySize: Int
		get() = Char.binarySize
})
val Byte.Companion.binarizer: StaticBinarizer<Byte> by DummyField(object : StaticBinarizer<Byte> {
	override fun binarize(bin: Binary, value: Byte) = bin.put(value)
	override fun debinarize(bin: Binary) = bin.byte()
	override val binarySize: Int
		get() = Byte.binarySize
})
val Short.Companion.binarizer: StaticBinarizer<Short> by DummyField(object : StaticBinarizer<Short> {
	override fun binarize(bin: Binary, value: Short) = bin.put(value)
	override fun debinarize(bin: Binary) = bin.short()
	override val binarySize: Int
		get() = Short.binarySize
})
val Int.Companion.binarizer: StaticBinarizer<Int> by DummyField(object : StaticBinarizer<Int> {
	override fun binarize(bin: Binary, value: Int) = bin.put(value)
	override fun debinarize(bin: Binary) = bin.int()
	override val binarySize: Int
		get() = Int.binarySize
})
val Long.Companion.binarizer: StaticBinarizer<Long> by DummyField(object : StaticBinarizer<Long> {
	override fun binarize(bin: Binary, value: Long) = bin.put(value)
	override fun debinarize(bin: Binary) = bin.long()
	override val binarySize: Int
		get() = Long.binarySize
})
val Float.Companion.binarizer: StaticBinarizer<Float> by DummyField(object : StaticBinarizer<Float> {
	override fun binarize(bin: Binary, value: Float) = bin.put(value)
	override fun debinarize(bin: Binary) = bin.float()
	override val binarySize: Int
		get() = Float.binarySize
})
val Double.Companion.binarizer: StaticBinarizer<Double> by DummyField(object : StaticBinarizer<Double> {
	override fun binarize(bin: Binary, value: Double) = bin.put(value)
	override fun debinarize(bin: Binary) = bin.double()
	override val binarySize: Int
		get() = Double.binarySize
})
val String.Companion.binarizer: Binarizer<String> by DummyField(object : Binarizer<String> {
	override fun binarize(bin: Binary, value: String) = bin.put(value)
	override fun debinarize(bin: Binary) = bin.string()
	override fun measure(value: String) = Binary.measure(value)
})

inline val BooleanArray.binarySize: Int
	get() = Int.binarySize + Boolean.binarySize * size
inline val CharArray.binarySize: Int
	get() = Int.binarySize + Char.binarySize * size
inline val ByteArray.binarySize: Int
	get() = Int.binarySize + Byte.binarySize * size
inline val ShortArray.binarySize: Int
	get() = Int.binarySize + Short.binarySize * size
inline val IntArray.binarySize: Int
	get() = Int.binarySize + Int.binarySize * size
inline val LongArray.binarySize: Int
	get() = Int.binarySize + Long.binarySize * size
inline val FloatArray.binarySize: Int
	get() = Int.binarySize + Float.binarySize * size
inline val DoubleArray.binarySize: Int
	get() = Int.binarySize + Double.binarySize * size

class Binary(val array: ByteArray, var index: Int = 0, val endIndex: Int = array.size) {
	companion object {
		val TYPE_MAP = mutableMapOf<KClass<*>, Binarizer<*>>(
			Boolean::class to Boolean.binarizer,
			Char::class to Char.binarizer,
			Byte::class to Byte.binarizer,
			Short::class to Short.binarizer,
			Int::class to Int.binarizer,
			Long::class to Long.binarizer,
			Float::class to Float.binarizer,
			Double::class to Double.binarizer,
			String::class to String.binarizer
		)

		inline fun <reified T> register(binarizer: Binarizer<T>) {
			TYPE_MAP[T::class] = binarizer
		}

		@Suppress("UNCHECKED_CAST")
		inline fun <reified T> binarizer(): Binarizer<T> = TYPE_MAP[T::class] as Binarizer<T>

		@Suppress("UNCHECKED_CAST")
		inline fun <reified T> staticBinarizer(): StaticBinarizer<T> = TYPE_MAP[T::class] as StaticBinarizer<T>

		inline fun <reified T> measureList(value: List<T>): Int = measureList(value, binarizer())
		inline fun <reified T> measureList(value: List<T>, binarizer: Binarizer<T>): Int =
			Int.binarySize +
					(if (binarizer is StaticBinarizer) binarizer.binarySize * value.size
					else value.sumBy { binarizer.measure(it) })

		inline fun <reified T> listBinarizer(): Binarizer<List<T>> = listBinarizer(binarizer())
		inline fun <reified T> listBinarizer(binarizer: Binarizer<T>): Binarizer<List<T>> =
			object : Binarizer<List<T>> {
				override fun binarize(bin: Binary, value: List<T>) = bin.putList(value, binarizer)
				override fun debinarize(bin: Binary): List<T> = bin.readList(binarizer)
				override fun measure(value: List<T>): Int = measureList(value, binarizer)
			}

		inline fun <reified K, reified V> measureMap(value: Map<K, V>): Int =
			measureMap(value, binarizer(), binarizer())

		fun <K, V> measureMap(value: Map<K, V>, keyBinarizer: Binarizer<K>, valueBinarizer: Binarizer<V>): Int =
			Int.binarySize +
					(if (keyBinarizer is StaticBinarizer) keyBinarizer.binarySize * value.size
					else value.keys.sumBy { keyBinarizer.measure(it) }) +
					(if (valueBinarizer is StaticBinarizer) valueBinarizer.binarySize * value.size
					else value.values.sumBy { valueBinarizer.measure(it) })

		inline fun <K, V> mapBinarizer(keyBinarizer: Binarizer<K>, valueBinarizer: Binarizer<V>) =
			object : Binarizer<Map<K, V>> {
				override fun binarize(bin: Binary, value: Map<K, V>) =
					bin.putMap(value, keyBinarizer, valueBinarizer)

				override fun debinarize(bin: Binary): Map<K, V> = bin.readMap(keyBinarizer, valueBinarizer)
				override fun measure(value: Map<K, V>): Int = measureMap(value, keyBinarizer, valueBinarizer)
			}

		inline fun <reified T> measure(value: T) = binarizer<T>().measure(value)

		fun measure(value: String) = Int.binarySize + value.sumBy {
			when {
				it < 0x0080.toChar() -> 1
				it < 0x0800.toChar() -> 2
				else -> 3
			}
		}

		inline fun <T> measure(value: T, binarizer: Binarizer<T>) = binarizer.measure(value)

		inline fun <reified T> binarize(value: T): ByteArray = binarize(value, binarizer())
		inline fun <T> binarize(value: T, binarizer: Binarizer<T>): ByteArray {
			return ByteArray(binarizer.measure(value)).also { binarizer.binarize(it.asBinary(), value) }
		}

		inline fun <T> debinarize(data: ByteArray, binarizer: Binarizer<T>): T =
			binarizer.debinarize(data.asBinary())
	}

	inline fun <R> require(count: Int, block: () -> R): R {
		require(index + count <= endIndex) { "requires $count bytes" }
		return block()
	}

	inline fun <reified T> read(): T = binarizer<T>().debinarize(this)
	fun <T> read(binarizer: Binarizer<T>): T = binarizer.debinarize(this)

	inline fun <reified T> readList() = readList(binarizer<T>())
	inline fun <reified T> readList(binarizer: Binarizer<T>): List<T> =
		Array(int()) { read(binarizer) }.asList()

	inline fun <reified T> readMutableList() = readMutableList(binarizer<T>())
	fun <T> readMutableList(binarizer: Binarizer<T>): MutableList<T> =
		MutableList(int()) { read(binarizer) }

	inline fun <reified K, reified V> readMap() =
		readMutableMap<K, V>(binarizer(), binarizer())

	inline fun <K, V> readMap(keyBinarizer: Binarizer<K>, valueBinarizer: Binarizer<V>) =
		readMutableMap(keyBinarizer, valueBinarizer)

	inline fun <reified K, reified V> readMutableMap() =
		readMutableMap<K, V>(binarizer(), binarizer())

	fun <K, V> readMutableMap(keyBinarizer: Binarizer<K>, valueBinarizer: Binarizer<V>): Map<K, V> =
		mutableMapOf<K, V>().apply {
			repeat(int()) {
				this[read(keyBinarizer)] = read(valueBinarizer)
			}
		}

	inline fun string(): String = String(byteArray())
	inline fun boolean(): Boolean = byte() != 0.toByte()
	inline fun char(): Char = short().toChar()

	inline fun byte(): Byte = require(1) { array[index++] }
	inline fun short(): Short = require(2) {
		(((byte().toInt() and 0xFF) shl 8) or
				(byte().toInt() and 0xFF)).toShort()
	}

	fun int(): Int = require(4) {
		((byte().toInt() and 0xFF) shl 24) or
				((byte().toInt() and 0xFF) shl 16) or
				((byte().toInt() and 0xFF) shl 8) or
				(byte().toInt() and 0xFF)
	}

	fun long(): Long = require(8) {
		((byte().toLong() and 0xFF) shl 56) or
				((byte().toLong() and 0xFF) shl 48) or
				((byte().toLong() and 0xFF) shl 40) or
				((byte().toLong() and 0xFF) shl 32) or
				((byte().toLong() and 0xFF) shl 24) or
				((byte().toLong() and 0xFF) shl 16) or
				((byte().toLong() and 0xFF) shl 8) or
				(byte().toLong() and 0xFF)
	}

	fun float(): Float = Float.fromBits(int())
	fun double(): Double = Double.fromBits(long())

	inline fun booleanArray(): BooleanArray = BooleanArray(int()) { boolean() }
	inline fun charArray(): CharArray = CharArray(int()) { char() }
	inline fun byteArray(): ByteArray =
		int().let { require(it) { array.copyOfRange(index, index + it).apply { index += it } } }

	inline fun shortArray(): ShortArray = ShortArray(int()) { short() }
	inline fun intArray(): IntArray = IntArray(int()) { int() }
	inline fun longArray(): LongArray = LongArray(int()) { long() }
	inline fun floatArray(): FloatArray = FloatArray(int()) { float() }
	inline fun doubleArray(): DoubleArray = DoubleArray(int()) { double() }

	inline fun <reified T> put(value: T) = binarizer<T>().binarize(this, value)
	inline fun <T> put(value: T, binarizer: Binarizer<T>) = binarizer.binarize(this, value)

	inline fun <reified T> putList(list: List<T>) = putList(list, binarizer())

	inline fun <T> putList(value: List<T>, binarizer: Binarizer<T>) {
		put(value.size)
		for (element in value) put(element, binarizer)
	}

	inline fun <reified K, reified V> putMap(value: Map<K, V>) =
		putMap(value, binarizer(), binarizer())

	inline fun <K, V> putMap(value: Map<K, V>, keyBinarizer: Binarizer<K>, valueBinarizer: Binarizer<V>) {
		put(value.size)
		for (pair in value) {
			put(pair.key, keyBinarizer)
			put(pair.value, valueBinarizer)
		}
	}

	inline fun put(value: String) = put(value.toByteArray())
	inline fun put(value: Boolean) = require(1) {
		put(if (value) 1.toByte() else 0.toByte())
	}

	inline fun put(value: Char) = put(value.toShort())

	inline fun put(value: Byte) = require(1) { array[index++] = value }
	inline fun put(value: Short) = require(2) {
		val int = value.toInt()
		put((int ushr 8).toByte())
		put(int.toByte())
	}

	fun put(value: Int) = require(4) {
		put((value ushr 24).toByte())
		put((value ushr 16).toByte())
		put((value ushr 8).toByte())
		put(value.toByte())
	}

	fun put(value: Long) = require(8) {
		put((value ushr 56).toByte())
		put((value ushr 48).toByte())
		put((value ushr 40).toByte())
		put((value ushr 32).toByte())
		put((value ushr 24).toByte())
		put((value ushr 16).toByte())
		put((value ushr 8).toByte())
		put(value.toByte())
	}

	fun put(value: Float) = put(value.toRawBits())
	fun put(value: Double) = put(value.toRawBits())

	fun put(value: BooleanArray) {
		put(value.size)
		for (v in value) put(v)
	}

	fun put(value: CharArray) {
		put(value.size)
		for (v in value) put(v)
	}

	fun put(value: ByteArray) {
		put(value.size)
		value.copyInto(array, index)
		index += value.size
	}

	fun put(value: ShortArray) {
		put(value.size)
		for (v in value) put(v)
	}

	fun put(value: IntArray) {
		put(value.size)
		for (v in value) put(v)
	}

	fun put(value: LongArray) {
		put(value.size)
		for (v in value) put(v)
	}

	fun put(value: FloatArray) {
		put(value.size)
		for (v in value) put(v)
	}

	fun put(value: DoubleArray) {
		put(value.size)
		for (v in value) put(v)
	}
}

inline fun ByteArray.asBinary(startIndex: Int = 0, endIndex: Int = size) = Binary(this, startIndex, endIndex)

inline fun <reified T> ByteArray.debinarize(): T = asBinary().read()
inline fun <reified T : Binarizable> T.binarize(): ByteArray = Binary.binarize(this, Binary.binarizer())
inline val <reified T : Binarizable> T.binarySize: Int
	inline get() = Binary.binarizer<T>().measure(this)
