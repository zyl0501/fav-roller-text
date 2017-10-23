# FavRollerText
仿即刻的点赞效果

<img src="https://github.com/zyl0501/fav-roller-text/blob/master/screenshot.gif"> 

如何使用

在项目级别的 build.gradle :
```java
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
} 
```

在app级别的 build.gradle:
```java
dependencies {
    compile 'com.github.zyl0501:fav-roller-text:1.0.0'
}  
```

1.FavImage

Method | Summary
--- | ---
`setSelected(boolean)` | 设置是否点赞
`setSelected(boolean, boolean)` | 设置是否点赞，并控制动画

2.RollerText

Method | Summary
--- | ---
`setText(CharSequence)` | 设置文本内容
`setText(CharSequence, boolean)` | 设置文本内容，并控制动画
`setTextSize(int)` | 设置文本大小
`setTextColor(int)` | 设置文本颜色
`setAnimDuration(int)` | 设置滚动的动画时间
`setComparator(Comparator<CharSequence>)` | 设置内容比较器，用于控制上下滚动
