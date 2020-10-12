package org.simple.clinic.fileprovider

import androidx.core.content.FileProvider

/**
 * It's better to have an explicit file provider for the application
 * to avoid any conflicts from the libraries.
 * https://commonsware.com/blog/2017/06/27/fileprovider-libraries.html
 */
class SimpleFileProvider : FileProvider()
