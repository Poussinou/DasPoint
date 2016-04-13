# DasPoint

## Overview

This Android app is a try to make a fast, full-featured and usable [Point.im](http://point.im) REST API client. Its functions are based on the possibilities of `point.im HTTP API`. 

![screenshot1](https://img-fotki.yandex.ru/get/40801/30443188.10/0_15e815_a902374a_L.jpg) ![screenshot2](https://img-fotki.yandex.ru/get/35827/30443188.10/0_15e817_efc07d17_L.jpg) 

![screenshot3](https://img-fotki.yandex.ru/get/135639/30443188.10/0_15e818_14366854_L.jpg) ![screenshot4](https://img-fotki.yandex.ru/get/60380/30443188.10/0_15e814_df540ce1_L.jpg)
## Thanks
A few lines of [this](https://github.com/Tishka17/Point.im-Android) code were used as a source for ideas, but essentially remastered. Thanks [Tishka](https://github.com/Tishka17) for inspiration. 

This app also couldn't live without some helpful libs:
- com.commit451:PhotoView
- com.squareup.okhttp3:okhttp
- com.nostra13.universalimageloader:universal-image-loader

and others, listed in [`app/build.gradle`](https://github.com/torgash-ivanblch/DasPoint/blob/master/app/build.gradle). 

## License information
[WTFPL](http://www.wtfpl.net)
Do wtf you want with this code. I don't care. 

## About pull requests
Everyone is welcome with pull requests. We can make the world better. But first, ask me to commit and push my own changes so that your ones would be checked for actuality. 

## WARNINGS
### Unstable
This app may misbehave in some situations. Until it's marked as stable, use at your own risk. When done, use at your own risk, too. 

## TODO (in Russian)
- создание поста
- инфо о пользователе
- цитирование комментариев
- S, U, BL, WL
- удаление поста/коммента (если разберемся с API DELETE)
- копирование комментов в буфер обмена
- продвинутый полноэкранный просмотр картинок
- выпилить загрузку GIF-анимации не по требованию
- создать хоть какие-то настройки
- кеш, уменьшение прожорливости приложения по трафику
- поле "открыть пост по ID"
- log out с закрытием сессии

