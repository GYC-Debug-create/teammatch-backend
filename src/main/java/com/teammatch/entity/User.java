package com.teammatch.entity;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：user
 *
 * 注意：此表由 M3 模块负责维护
 * M5 模块只读取用户昵称、头像等基本信息，用于互评成员列表展示
 * 如果 M3 模块已创建此 Entity，后续可合并或删除此文件
 */
public class User {
    /** 用户ID */
    private Long id;

    /** 微信 openid */
    private String openid;

    /** 用户昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatarUrl;

    /** 学校邮箱 */
    private String email;

    /** 学校邮箱是否已验证：0 未验证，1 已验证 */
    private Boolean emailVerified;

    /** 学校名称 */
    private String school;

    /** 专业 */
    private String major;

    /** 年级 */
    private String grade;

    /** 个人简介 */
    private String bio;

    /** GitHub 用户名 */
    private String githubUsername;

    /** GitHub 是否已认领：0 未认领，1 已认领 */
    private Boolean githubClaimed;

    /** 正式档案是否完成：0 未完成，1 已完成 */
    private Boolean formalProfileCompleted;

    /** 信誉分缓存值，来源于 credit_change 表的有效记录 */
    private Integer creditScore;

    /** 用户角色：user 普通用户，admin 管理员 */
    private String role;

    /** 用户状态：active 正常，banned 封禁 */
    private String status;

    /** 管理员用户名 */
    private String username;

    /** 管理员密码哈希 */
    private String passwordHash;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public Boolean getGithubClaimed() {
        return githubClaimed;
    }

    public void setGithubClaimed(Boolean githubClaimed) {
        this.githubClaimed = githubClaimed;
    }

    public Boolean getFormalProfileCompleted() {
        return formalProfileCompleted;
    }

    public void setFormalProfileCompleted(Boolean formalProfileCompleted) {
        this.formalProfileCompleted = formalProfileCompleted;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
