/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package wiki.moderator.bot.general.db.entities

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.result.UpdateResult
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import wiki.moderator.bot.general.db.MongoDB

@Serializable
@Suppress("DataClassContainsFunctions", "DataClassShouldBeImmutable")
internal data class MetadataEntity(
	@Contextual
	override val _id: String,

	var version: Int,
) : Entity<String> {
	suspend inline fun save(): UpdateResult =
		save(this)

	companion object {
		const val COLLECTION_NAME: String = "metadata"

		val codec = KotlinSerializerCodec.create<MetadataEntity>()

		private val FilterFunctions = object {
			fun byId(id: String) =
				eq(MetadataEntity::_id.name, id)
		}

		private val collection by lazy {
			MongoDB.getCollection<MetadataEntity>(COLLECTION_NAME)
		}

		suspend fun get(id: String): Int? =
			collection
				.find<MetadataEntity>(FilterFunctions.byId(id))
				.limit(1)
				.firstOrNull()
				?.version

		suspend fun set(id: String, version: Int): UpdateResult =
			save(MetadataEntity(id, version))

		suspend fun save(document: MetadataEntity): UpdateResult =
			collection
				.replaceOne(
					FilterFunctions.byId(document._id),
					document,
					ReplaceOptions().upsert(true)
				)
	}
}
