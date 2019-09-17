package org.simple.clinic.home.patients.illustration

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Observable

@Entity
data class HomescreenIllustration(
    @PrimaryKey
    val eventId: String,

    val illustrationUrl: String,

    @Embedded(prefix = "from_")
    val from: DayOfMonth,

    @Embedded(prefix = "to_")
    val to: DayOfMonth
) {

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM HomescreenIllustration")
    fun illustrations(): Observable<List<HomescreenIllustration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(homescreenIllustration: HomescreenIllustration)
  }
}
