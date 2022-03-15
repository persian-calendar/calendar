# Calendar
[![](https://jitpack.io/v/persian-calendar/calendar.svg)](https://jitpack.io/#persian-calendar/calendar)

Calendar converter based on http://code.google.com/p/mobile-persian-calendar/ (GPLv2) initially but changed a lot afterward.

  ```
  Copyright (C) 2006  Amir Khosroshahi (amir.khosroshahi)
  Copyright (C) 2012-2021  Android Persian Calendar

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  ```

# Usage

Add this in your root build.gradle at the end of repositories section:
```kotlin
allprojects {
    repositories {
        ...
        maven("https://jitpack.io")
    }
}
```

Now actually add the dependency:
```kotlin
dependencies {
    implementation("com.github.persian-calendar:calendar:1.2.0")
}
```
 
For other build tools support have a look at [this](https://jitpack.io/#persian-calendar/calendar).
