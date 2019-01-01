package com.leeeyou.wanandroid.model.bean

data class RecommendItem(val apkLink: String,
                         val author: String,
                         val chapterId: Int,
                         val chapterName: String,
                         val collect: Boolean = false,
                         val desc: String,
                         val envelopePic: String,
                         val fresh: Boolean = false,
                         val id: Int,
                         val link: String,
                         val niceDate: String,
                         val origin: String,
                         val projectLink: String,
                         val publishTime: Long,
                         val superChapterId: Int,
                         val superChapterName: String,
                         val tags: List<Tag>,
                         val title: String,
                         val type: Int = 0,
                         val userId: Int = -1,
                         val visible: Int = 1,
                         val zan: Long = 0)