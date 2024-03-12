/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package wiki.moderation.bot.general.db.entities

import com.kotlindiscord.kord.extensions.modules.extra.tags.data.Tag
import com.kotlindiscord.kord.extensions.modules.extra.tags.data.TagsData
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.result.UpdateResult
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import org.bson.conversions.Bson
import wiki.moderation.bot.general.db.MongoDB

@Serializable
@Suppress("DataClassContainsFunctions", "DataClassShouldBeImmutable")
internal object TagEntity : TagsData {
	const val COLLECTION_NAME: String = "tags"

	val codec = KotlinSerializerCodec.create<Tag>()

	private val FilterFunctions = object {
		fun guildOrNull(guildId: Snowflake?) =
			Filters.or(
				eq(Tag::guildId.name, null),
				eq(Tag::guildId.name, guildId),
			)

		fun byKey(key: String, guildId: Snowflake?) =
			Filters.and(
				guildOrNull(guildId),
				eq(Tag::key.name, key.lowercase())
			)

		fun byKeySpecific(key: String, guildId: Snowflake?) =
			Filters.and(
				eq(Tag::guildId.name, guildId),
				eq(Tag::key.name, key.lowercase())
			)

		fun byCategory(category: String, guildId: Snowflake?) =
			Filters.and(
				guildOrNull(guildId),
				eq(Tag::category.name, category.lowercase())
			)
	}

	private val collection by lazy {
		MongoDB.getCollection<Tag>(COLLECTION_NAME)
	}

	suspend fun save(document: Tag): UpdateResult =
		collection
			.replaceOne(
				FilterFunctions.byKeySpecific(document.key, document.guildId),
				document,
				ReplaceOptions().upsert(true)
			)

	override suspend fun deleteTagByKey(key: String, guildId: Snowflake?): Tag? =
		collection
			.findOneAndDelete(FilterFunctions.byKeySpecific(key, guildId))

	override suspend fun findTags(category: String?, guildId: Snowflake?, key: String?): List<Tag> {
		val criteria = mutableListOf<Bson>()

		if (category != null) {
			criteria.add(eq(Tag::category.name, category.lowercase()))
		}

		if (guildId != null) {
			criteria.add(eq(Tag::guildId.name, guildId))
		}

		if (key != null) {
			criteria.add(eq(Tag::key.name, key.lowercase()))
		}

		@Suppress("SpreadOperator")
		return if (criteria.isEmpty()) {
			collection
				.find<Tag>()
				.toList()
		} else if (criteria.size == 1) {
			collection
				.find<Tag>(criteria.first())
				.toList()
		} else {
			collection
				.find<Tag>(Filters.and(*criteria.toTypedArray()))
				.toList()
		}
	}

	override suspend fun getAllCategories(guildId: Snowflake?): Set<String> =
		// NOTE: Can't use collection.distinct() due to poor support for kx.ser in the driver.
		collection
			.find<Tag>(FilterFunctions.guildOrNull(guildId))
			.map { it.category }
			.toSet()

	override suspend fun getTagByKey(key: String, guildId: Snowflake?): Tag? =
		collection
			.find<Tag>(FilterFunctions.byKey(key, guildId))
			.limit(1)
			.firstOrNull()

	override suspend fun getTagsByCategory(category: String, guildId: Snowflake?): List<Tag> =
		collection
			.find<Tag>(FilterFunctions.byCategory(category, guildId))
			.toList()

	override suspend fun getTagsByPartialKey(partialKey: String, guildId: Snowflake?): List<Tag> =
		// NOTE: Can't use regex filters due to poor support for kx.ser in the driver.
		collection
			.find<Tag>(FilterFunctions.guildOrNull(guildId))
			.filter { tag -> tag.key.contains(partialKey, ignoreCase = true) }
			.toList()

	override suspend fun getTagsByPartialTitle(partialTitle: String, guildId: Snowflake?): List<Tag> =
		// NOTE: Can't use regex filters due to poor support for kx.ser in the driver.
		collection
			.find<Tag>(FilterFunctions.guildOrNull(guildId))
			.filter { tag -> tag.title.contains(partialTitle, ignoreCase = true) }
			.toList()

	override suspend fun setTag(tag: Tag) {
		save(tag)
	}
}
