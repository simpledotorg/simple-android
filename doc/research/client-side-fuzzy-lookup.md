# Client-side "Fuzzy" Lookup in SQLite

## Goals
- What are the options we have to improve patient lookup on the client side
- What are the tradeoffs we are making with each approach
- How much effort is required to use any of these approaches

## TL;DR
- Ignoring substrings, punctuation and substring search is relatively low effort and can be implemented together
- Spelling variation correction is high effort, will add around 1.5 MB to the APK size, will require us to change some of our build processes but will give significantly better results and can be configured to do corrections for multiple languages

## Details
### Suggestions for improving lookups
The following suggestions were put forward by the team as possible things we can do to improve patient lookup:
1. Ignore spaces and punctuation in patient name
2. Look for substrings in the patient name instead of the complete name
3. Use Levenshtein Distance to surface results that may be lost due to spelling variations

### Fuzzy Lookup
Since we use SQLite to store data locally, an ideal solution for fuzzy lookup is one that will work in the SQLite layer itself. Looking at the official [documentation](https://www.sqlite.org/lang_expr.html) (See section titled `The LIKE, GLOB, REGEXP, and MATCH operators`), it seems the best option is using the **LIKE** clause to look for patient names matching a particular pattern.

The **LIKE** clause will handle substring matches out of the box, but not punctuation and spaces. To handle this, we need to do things:
- Create a new column in the Patient table that will hold a copy of the patient's full name, but stripped of all punctuation and spaces
- When passing the user entered string into the SQL query when searching for patients, strip it of all punctuation and spaces as well and query against the search column we maintain for each patient

### Spelling Variations
Spelling variation detection can be done in a lot of ways; For the purposes of this document, we have limited it to the correction that can be done in the SQLite layer on Android.

SQLite has an extension, [`Spellfix1`](https://www.sqlite.org/spellfix1.html) that gives spelling correction support. This has features we can use to provide spelling variation detection. However, it is not possible to use this extension out of the box on Android, mainly because
1. It is a dynamic extension, i.e, it has to be loaded dynamically into the SQLite instance
2. SQLite has to be compiled with support for loading dynamic extensions, and the SQLite lib integrated into Android does not include support for them.

The only option for getting support for this in the app, is to bundle the app with our own build of SQLite with dynamic extension support turned on.

#### Community Options
There is a community supported version of SQLite that has support for loading extensions and is also kept up to date with new SQLite releases, maintained [here](https://github.com/requery/sqlite-android). If we use this, we can maintain and build `spellfix1` ourselves and bundle it with the app. This approach, however, does come with its own cons, i.e
1. Bundling SQLite in our app, will increase the APK size significantly (~6-7 MB). This is because the APK is currently bundled with different native libs for ALL CPU architectures. We can split it by the CPU architectures and generate multiple APKs by architecture, which will require significant changes to our build, deployment setup on Bitrise. Even then, we are still looking at an APK size increase of around 1.5 MB.
2. We will have to maintain `spellfix1` ourselves, and release new versions every time a new SQLite release happens and (if) we choose to upgrade to it. This will probably not be a problem since we can just pin a version of SQLite and not upgrade to newer versions and just compile `spellfix1` once.

#### Spellfix1
Even after integrating `spellfix1`, we still have two options that we can use to do spelling variation detection:

##### Levenshtein Distance (Edit Distance)
`spellfix1` provides an Edit Distance sql function (`editdist3`) that can be used in standard `WHERE` clauses to match names with possible spelling errors. Using this, we can easily run queries like `select *, editdist3 ('pattern', fullName) as editDist from Patient where editDist < 1000 order by editDist asc` which will give us results ordered by the likelihood of a match. The number `1000` is used based on a predefined cost table (provided and used by `spellfix1`) for transforming characters of a character set and will have to be figured out through trial and error.

##### Spellfix virtual table
We also have the option to create and maintain a separate table used for the sole purpose of spelling correction. This requires a bit more effort than just using the Edit Distance, but also gives us better results by using phonetic matching (Based on concepts from Soundex and Metaphone) on top of edit distance.

Both the Edit Distance and Spellfix virtual table methods come with out of the box support for English. We do have the capability to add edit distance costs for non-English languages, but it will have to be done on a per-language basis, and the effort involved in creating and maintaining these costs is unclear, as well as how well the spelling correction will work for indic languages. This might need to be researched separately.

## POC
There is a proof-of-concept project [HERE](https://github.com/vinaysshenoy/SQLiteSpellfix) that uses Fuzzy Lookup, Levenshtein Distance and Spellfix to show the kind of fuzzy lookup that is possible (Latin characters only).

##### Questions that need to be answered
- For implementing the fuzzy lookup in beta20, do we need to migrate the data that already exists on the current installs, or can we force an uninstall for all the users

