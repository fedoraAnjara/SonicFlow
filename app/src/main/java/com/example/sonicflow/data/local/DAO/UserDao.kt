package com.example.sonicflow.data.local.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.sonicflow.data.local.Entity.UserEntity

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user WHERE email = :email AND password = :password")
    suspend fun getUserByEmailAndPassword(email: String, password: String): UserEntity?

    @Query("SELECT * FROM user WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
}