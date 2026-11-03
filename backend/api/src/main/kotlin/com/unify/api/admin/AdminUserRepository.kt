package com.unify.api.admin

import org.springframework.data.jpa.repository.JpaRepository

interface AdminUserRepository : JpaRepository<AdminUser, String>
