package org.kiot.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

internal class BinaryTest {
	data class Apple(val name: String, val price: Double) : Binarizable {
		companion object {
			val binarizer = object : Binarizer<Apple> {
				override fun binarize(bin: Binary, value: Apple) = value.run {
					bin.put(name)
					bin.put(price)
				}

				override fun debinarize(bin: Binary) = Apple(bin.string(), bin.double())
				override fun measure(value: Apple): Int = Binary.measure(value.name) + Double.binarySize
			}.also { Binary.register(it) }
		}
	}

	data class Point(val x: Float, val y: Float) : Binarizable {
		companion object {
			val binarizer = object : StaticBinarizer<Point> {
				override fun binarize(bin: Binary, value: Point) = value.run {
					bin.put(x)
					bin.put(y)
				}

				override fun debinarize(bin: Binary) = Point(bin.float(), bin.float())
				override val binarySize: Int
					get() = Double.binarySize * 2
			}.also { Binary.register(it) }

			val binarySize: Int
				get() = binarizer.binarySize
		}
	}

	@Test
	fun test() {
		val apple = Apple("Apple A", 3.95)
		assertEquals(
			apple,
			apple.binarize().debinarize()
		)
		assertEquals(
			Binary.measure(apple.name) + Double.binarySize,
			apple.binarySize
		)
		assertFails {
			val array = apple.binarize()
			array.asBinary(0, array.size - 1).read(Apple.binarizer)
		}
		run {
			val array = apple.binarize()
			assertEquals(
				array.asBinary().read(Apple.binarizer),
				array.asBinary().read()
			)
		}
	}

	@Test
	fun testList() {
		val list = listOf(
			Apple("Apple A", 3.95),
			Apple("Apple B", 4.75)
		)
		Binary.listBinarizer(Apple.binarizer).let {
			assertEquals(
				list,
				Binary.binarize(list, it).asBinary().read(it)
			)
			Binary.register(it)
			assertEquals(
				list,
				Binary.binarize(list).debinarize()
			)
		}
	}

	@Test
	fun testMap() {
		val map = mapOf(
			"A" to Apple("A", 3.95),
			"B" to Apple("B", 4.83)
		)
		Binary.mapBinarizer(String.binarizer, Apple.binarizer).let {
			assertEquals(
				map,
				Binary.binarize(map, it).asBinary().read(it)
			)
			Binary.register(it)
			assertEquals(
				map,
				Binary.binarize(map).debinarize()
			)
		}
	}

	@Test
	fun testStatic() {
		val point = Point(1.0f, 2.5f)
		assertEquals(
			Point.binarySize,
			point.binarySize
		)
		assertEquals(
			Point.binarySize,
			point.binarize().size
		)
	}
}