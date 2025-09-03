const MAVEN_BUILD_TIMESTAMP = "2025-09-03T13:52:39Z";

console.log("PWA mode is on - mavenBuildTimestamp = " + MAVEN_BUILD_TIMESTAMP);

const CACHE_NAME = "webfx-pwa-cache";
const DEFAULT_PRE_CACHE = false;

// Single asset map that PwaMojo will populate: { "/file1": { preCache: true|false, hash: "XXXX" }, "/file2": "YYYY", ... }
// If preCache is missing, or the value is a string (treated as a hash), we consider preCache = DEFAULT_PRE_CACHE
const ASSET = {
  "/8D097A97B26CF53E9F4F520852640D42.cache.js": "721a046997e1ab75a78e8df9a7ca21af3953741191978ccda1c2c05081aa39a3",
  "/AppIcon-1024x1024.png": "d92047008fbd04db13315896bc1f31d973eb70f52091dc95f68229b99aae1d1c",
  "/dev/webfx/kit/css/main.css": "afafb84677fe281db7c302b25cb2acd035e3db4257da208f38ebdcb92343884e",
  "/dev/webfx/kit/mapper/peers/javafxcontrols/gwt/html/perfect-scrollbar.css": "7b6508c9e8e04de8ebfec5de2ce1c4303bc46a0a279283eff7e248c1c900a91b",
  "/dev/webfx/kit/mapper/peers/javafxcontrols/gwt/html/perfect-scrollbar.externs.js": "95a3f9084f5ca536168b60ffa143446e8ed6f9271111f92e4d0b0d56385c403a",
  "/dev/webfx/kit/mapper/peers/javafxcontrols/gwt/html/perfect-scrollbar.js": "dcdccf78523537e1188b5a173b48d497e243891f3aeddaf1e79eb5d10ee3088f",
  "/dev/webfx/kit/mapper/peers/javafxcontrols/gwt/html/perfect-scrollbar.min.js": "3535c240c3cfef15b9fd77b610c9b41db9ff2f4569949230b7f15a9c8cd0b6e7",
  "/emul/java/util/regex/xregexp.js": "263ed3cef90a59f0a94fbc2e54dfd0c32526df337132e361e0e9303502828f58",
  "/eu/hansolo/spacefx/CityStomper.mp3": "64bedb96b8ff1c2101c86dfacb1c5f37c1825df9f68a7d575613b0da9b722cbf",
  "/eu/hansolo/spacefx/RaceToMars.mp3": "d2b29e881ac10030613cad6272eaace6bdcad23f39e07d66fc5690382bf15c08",
  "/eu/hansolo/spacefx/Spaceboy.ttf": "f2fce21a778bd39b47315eab1fd638e91ab5d4b71413f77aa205ae9ee5e0cf6f",
  "/eu/hansolo/spacefx/asteroid1.png": "fbd95a39e676888162e5991239492b9a5115725c1c659e22f69e6b7fbd9f51f1",
  "/eu/hansolo/spacefx/asteroid10.png": "a9b33dcd5e8703f358f1df49883d792661cf54473af099e5ea20a34922c7588c",
  "/eu/hansolo/spacefx/asteroid11.png": "3a84c946145a05cb6f4b44bcd28721bc5a8379ab76670731b0a1d2f0437098a9",
  "/eu/hansolo/spacefx/asteroid2.png": "434cbd15e74a00b97af9ce76a7e498f9bc1e493fe9b868e953c66a2f3ecb5a77",
  "/eu/hansolo/spacefx/asteroid3.png": "82e1736109d5c48b14db20d9f252601f10d406e50a93ad2fe7a26d839f5c44c8",
  "/eu/hansolo/spacefx/asteroid4.png": "ae6221ea13435395c81f79f7c406df6cb63845d220f72a26f275d9c2f8f58d82",
  "/eu/hansolo/spacefx/asteroid5.png": "ada1ca3dbecf15ca68ec893d51839b3ef5d96221810474bff1207b7524d0bb13",
  "/eu/hansolo/spacefx/asteroid6.png": "24e76f3e16f08e330ef2634267c2545b843e31e63b046d5676b5387e3cb2722c",
  "/eu/hansolo/spacefx/asteroid7.png": "02c5eacc4d4ca7b7191592391fb6b0b356ded4f1eb34b219bcf7ced8788fabdc",
  "/eu/hansolo/spacefx/asteroid8.png": "55d1e898f0835a6165d7659a4fc204d700a233b06642db7e89722387e16b1457",
  "/eu/hansolo/spacefx/asteroid9.png": "5da36fa1d85a8335f99a345eb70314246790561caa03ee6b5da832ae972be3aa",
  "/eu/hansolo/spacefx/asteroidExplosion.mp3": "3bc4607b564566a594c5e076bc439ba6b2f108eb4b3414241414c2d93e0de8cc",
  "/eu/hansolo/spacefx/asteroidExplosion.png": "3a12b12d95bca550d346898f84a9703350f9419e32228dca971f803680f1b8dd",
  "/eu/hansolo/spacefx/backgroundL1.jpg": "414fc0971d12bc560ac5b3e7cede8ca61a7e5d04f9954f68734ecd038d213d02",
  "/eu/hansolo/spacefx/backgroundL2.jpg": "3d0ad6386640c6f2b76a419473af921ad16e7a95d3e9aa415693e600f3a6683c",
  "/eu/hansolo/spacefx/backgroundL3.jpg": "69b9708d922060f5dc49dd50584f500a88f06a97d17a1da9ecd95d876b30736e",
  "/eu/hansolo/spacefx/bigTorpedoBonus.png": "e291244bcf7e51c4baadb42b4c2aa2f8193021198e847fd797033438d22abe5b",
  "/eu/hansolo/spacefx/bigtorpedo.png": "586378eb1b2a4770b517e11f805e305150a3409884648531910d9cef35436285",
  "/eu/hansolo/spacefx/bigtorpedo360.png": "ef736ebe1b0e004abfaab629efaf8078f0f76f847495bd7c1a867c5794c779c8",
  "/eu/hansolo/spacefx/bonus.mp3": "7c63170ff0407c8d0e590eecc4d5fc778be060210f5517dad1f7e16eb16d4248",
  "/eu/hansolo/spacefx/crystal.png": "cb2bb4d703bdcbebffc5b89288152a5649af8a418a791c027a9f088535f5fbdc",
  "/eu/hansolo/spacefx/deflectorshield.png": "ac9dfa3a8d56720c40720349e0708d4834a0eee8ed600869eac7cd0cd523b0ff",
  "/eu/hansolo/spacefx/deflectorshieldSound.mp3": "f9ec797bc728d3b12609fdcb9a8fbd548d51e764b342e50d0e3315a515a972be",
  "/eu/hansolo/spacefx/enemy1L1.png": "634ca9cca6ce8152ac6cf062d248209c45e8378d6963134cdb1d45743c02df7d",
  "/eu/hansolo/spacefx/enemy1L2.png": "3851f99491779d62e106f65e5f0ab8dd80999286149b5839fbb9cc80894632d9",
  "/eu/hansolo/spacefx/enemy1L3.png": "c1905b984bb35b68bcffb5e81fdcc15201a5946e53233ae793ea70ceed448e4c",
  "/eu/hansolo/spacefx/enemy2L1.png": "28681a002dc895d53ab39fd717a7abb83c95fb8c7dc3829cc0cd8ee6ec709040",
  "/eu/hansolo/spacefx/enemy2L2.png": "0cd561dd73d8554c78d4987fed07d5e4427257bcd51ab899759a3ab61607f339",
  "/eu/hansolo/spacefx/enemy2L3.png": "f8dbc6aa44cd274d1bf635c346f5970b3af54958b05693910ae0ec1c30b6382b",
  "/eu/hansolo/spacefx/enemy3L1.png": "ef95041f4b512108d168b8c9329af07254202065d8f227982eac8bc410b99a67",
  "/eu/hansolo/spacefx/enemy3L2.png": "c5c67673012c09f2e39d6c025232aed6bbdb6ef71133545688e0336196e9d994",
  "/eu/hansolo/spacefx/enemy3L3.png": "63381cd19fcce4196645aace2d851702f11f30eaa94e816da017694cac05ff20",
  "/eu/hansolo/spacefx/enemy4L2.png": "1d8ece6220397c0cb15c97dde995157718bc6b42160eb610d156f1c87a349f16",
  "/eu/hansolo/spacefx/enemy4L3.png": "0718a1d91a9eaf25b9fc4f77d067845105b8b1ce6df6a372444940cbcaae4df0",
  "/eu/hansolo/spacefx/enemy5L2.png": "5a863b4dff3bf6c9a2ce23b69b35e8590af19561547852cd0da593e5f5e74ea7",
  "/eu/hansolo/spacefx/enemy5L3.png": "632be27cd11c35c9a1c6ab7275d13293d4785e2d25d275abccf6e7146d0cb26b",
  "/eu/hansolo/spacefx/enemy6L2.png": "287055d11adb93c8c5592a623a099237be204df66810aa2ce02c99efd4fe0dcc",
  "/eu/hansolo/spacefx/enemyBomb.mp3": "e778e36e1813bbc9090917c7dbb764ddf4bb4c28cde449d52823a12a09cfff53",
  "/eu/hansolo/spacefx/enemyBombL1.png": "4a3e3efb4177fa0ca71163ead10ad0092a931d14640795f923a7806803e226f8",
  "/eu/hansolo/spacefx/enemyBombL2.png": "b368e31ea3ada99303e0c1429b744c4b9ebb8e76e0aeeaf49d10ad8f8c2447a3",
  "/eu/hansolo/spacefx/enemyBombL3.png": "0eeb6ddf27da2154da306ab1181c05c0dac0002ba348b93745ce2a5cc65bbd83",
  "/eu/hansolo/spacefx/enemyBoss0L1.png": "3854208959434b5342be5362aaa815aa0370eb3eda60496f88a73a554b2f89bf",
  "/eu/hansolo/spacefx/enemyBoss0L2.png": "388e9d01cfc70789545156acb6cb8aa17d23b5f21ea99d4de29d7b18f03afec1",
  "/eu/hansolo/spacefx/enemyBoss0L3.png": "3e076b22f512e2739b3286921b24cba7adf4cf0119c912375d6dd1181ad03350",
  "/eu/hansolo/spacefx/enemyBoss1L1.png": "18a4e2985cecb17b626a3e0908877d454e5784326d54970cce85875be14b111e",
  "/eu/hansolo/spacefx/enemyBoss1L2.png": "08ba6db9ee534b4560f8b7e3c9464bcdd0827356a865a1bf8626e0340786fd23",
  "/eu/hansolo/spacefx/enemyBoss1L3.png": "43bfb73a8b06b307fcc55f1e0156712589a575686b7a03b116c7fb7298021e08",
  "/eu/hansolo/spacefx/enemyBoss2L1.png": "a85ff37ddcb0d2294d1af57edfcbf058915b202b1479487aaf0c00b809b7f4d4",
  "/eu/hansolo/spacefx/enemyBoss2L2.png": "aa412ca7c217f732ee33c81be8fde793790b6c7b4ed85e911f7af998c9d9b419",
  "/eu/hansolo/spacefx/enemyBoss2L3.png": "4763a58555f1cbfed993486020a47c1d02a3c00b32a36c260e7e79130abd9f2a",
  "/eu/hansolo/spacefx/enemyBoss3L1.png": "50505f5795c3bedf305d5679977aa9544d38967e505e88d2d10abcd56bac4792",
  "/eu/hansolo/spacefx/enemyBoss3L2.png": "af42705d6f040e30ddf860fd091efb1b9e0e7084dc8eeb78ec6dd5c6513eb8d6",
  "/eu/hansolo/spacefx/enemyBoss3L3.png": "65a61a7a5a4673eb4d7c698e886738087890024159ebfb1e7fd15b52c468d388",
  "/eu/hansolo/spacefx/enemyBoss4L1.png": "de36809e8faa6b040fae72515594cd929cc9e3bbe18cca2079bd3fa3767ad625",
  "/eu/hansolo/spacefx/enemyBoss4L2.png": "d0beab83e9cd812da4aa4f6da30f1c71fad2245969940564963398e319f9496c",
  "/eu/hansolo/spacefx/enemyBoss4L3.png": "dccc966bc3a03800c6934497d81624c51cc1e3efd701e4b7c315d64df815498a",
  "/eu/hansolo/spacefx/enemyBossExplosion.mp3": "0c16292b0109b7c81f4e470e009558268f3f4dc252a7f2537f4648d4a9e11b4b",
  "/eu/hansolo/spacefx/enemyBossExplosionL1.png": "5a30389199e9f9ed9e9ae5a9913f39e9d8c3d49b407b16661ed092cbd755fba1",
  "/eu/hansolo/spacefx/enemyBossExplosionL2.png": "f995530ae01d664e46723eb64211965f4db6d16b424fb3bf84c2bc3986092afd",
  "/eu/hansolo/spacefx/enemyBossExplosionL3.png": "b8b878cecac15179a7407cd7fb5a7771802df04eb50fe2db48701fbc4183d060",
  "/eu/hansolo/spacefx/enemyBossRocketL1.png": "350c7dfe92fc10d044652334ea3ccd3b3250721fe750bfb1c62033d59689e80a",
  "/eu/hansolo/spacefx/enemyBossRocketL2.png": "3e9883d803c5b17fdc2762fa952e8914c2184c4300d394fbc6d15c556ed8c1a3",
  "/eu/hansolo/spacefx/enemyBossRocketL3.png": "6f4fa818d6fa3f065f647d0f7d038f54ee500f074713a49e8f0971c66fc1b4c3",
  "/eu/hansolo/spacefx/enemyBossShieldHit.mp3": "4529a1172d60a80e038bbe8807d9d79d54522a8ca27b833c040cdd9d56b33948",
  "/eu/hansolo/spacefx/enemyBossTorpedoL1.png": "9d54f32b32a44c0e03381985cbe24ae4a52dc858685712806dc482ee9dc2bdef",
  "/eu/hansolo/spacefx/enemyBossTorpedoL2.png": "f4dbd0c08c6368ca7246b4969bc47195ffc5b873ca66231f8107ac874f309f42",
  "/eu/hansolo/spacefx/enemyBossTorpedoL3.png": "5f54014bbb823bb2591d35c532799c1748cac04d7c912634b631d74fe46c08a4",
  "/eu/hansolo/spacefx/enemyLaserSound.mp3": "0cd4c3c5ef59d7fbcb3035eef47cd14eaf3d9908e285de55a6ef588b83fa6703",
  "/eu/hansolo/spacefx/enemyRocketExplosionL1.png": "d0c347ce99ac1655a0cdeb7e41c419c60b56679c31a5048a8242947e3e13a30b",
  "/eu/hansolo/spacefx/enemyRocketExplosionL2.png": "ba76fa47c634a72f5fa04e5272f1824026d227da3543fbb4e1221eee6616ccd7",
  "/eu/hansolo/spacefx/enemyRocketExplosionL3.png": "2b744dbfafc39296f3a8cee8b36a37d416a612658c320d065674cfc2319c2ed3",
  "/eu/hansolo/spacefx/enemyTorpedoL1.png": "a2426b7cf3cc722da8ed3cf7da4578ae26bae4234535e44b9214b6e07494f213",
  "/eu/hansolo/spacefx/enemyTorpedoL2.png": "419f467a51827045b189abe636b211efc1c8822005c4b3c2f9e2c983ef7fbe09",
  "/eu/hansolo/spacefx/enemyTorpedoL3.png": "97b687eb038508fca730677a1b04d7ccc04d9865a07846876506605ea222a331",
  "/eu/hansolo/spacefx/explosionL1.png": "462506cbf0c6c494c7440343980018947a95576984a5f23ae7c84f8cb100d1a8",
  "/eu/hansolo/spacefx/explosionL2.png": "c1a4a54876a31334e5ac78a40001fa233c0978e2f6600e23c185a844e1ff417b",
  "/eu/hansolo/spacefx/explosionL3.png": "f7fc9f2d6c6eb5ce3a18e4ad42975c79d298560eb3169ddb16c6129ae593b99b",
  "/eu/hansolo/spacefx/explosionSound.mp3": "dec8ecb7cac1cfe7b162f39bcc7c2b49e52a19ae454a2d5ff0c7270e2479acc4",
  "/eu/hansolo/spacefx/explosionSound1.mp3": "4529a1172d60a80e038bbe8807d9d79d54522a8ca27b833c040cdd9d56b33948",
  "/eu/hansolo/spacefx/furyBonus.png": "370ed356972653a6eae2bd40a955a2a490d01e775f39c52a2b4b7061121563ff",
  "/eu/hansolo/spacefx/gameover.jpg": "a2c0a292185e15b4e493ba2fcbc5cec048d1defc0e7acaa138ea2baa5a793484",
  "/eu/hansolo/spacefx/gameover.mp3": "a0ae240a66c7e5ebabd925264bd21b8b213381ecb1d4d1afb3cba3ef9afa3817",
  "/eu/hansolo/spacefx/halloffamescreen.jpg": "e0e04de9e254074b112d2db83847ce4e0c85f49faa6d8e7247a2d7845d893233",
  "/eu/hansolo/spacefx/hit.mp3": "6e8aa17acea6775ec9832efd395e87e2806dddd5c0316435651c5a9f318afac1",
  "/eu/hansolo/spacefx/icon.png": "fc771f2ae14fdfddb2f39bbd880c746b3cf88e7bdb4eebc02b120d918faded48",
  "/eu/hansolo/spacefx/laserSound.mp3": "ed70d5844ad60bc91f7d9079398f1b0f667b7f61e00d1c391180dfe9b5b7507b",
  "/eu/hansolo/spacefx/levelBossBomb.mp3": "d9c36f5dbd0bbc426a42326dbb2ab96128220fa4cc0b8a7bbd065c910f5cd708",
  "/eu/hansolo/spacefx/levelBossBombL1.png": "2f9f159065d63b83e103979272af4d5a382e863a4db5b532d80fa393a5565791",
  "/eu/hansolo/spacefx/levelBossBombL2.png": "94f7481eaf4377f69d78527cc6c1f24254c1920643ce721024d97454eb0f2ef5",
  "/eu/hansolo/spacefx/levelBossBombL3.png": "dd5adbfca32140b100635aec5cdacf888ee0108ef50fb077d49c06c163d14150",
  "/eu/hansolo/spacefx/levelBossExplosionL1.png": "8bcf1ab0c049db4d849b1f0b6565bc1bc113ac27b13358c2021b6841d5085ec1",
  "/eu/hansolo/spacefx/levelBossExplosionL2.png": "b2b7e37840b3865007d2f18eec08a4b831c92592d2cbfe6681733df689f4a7e6",
  "/eu/hansolo/spacefx/levelBossExplosionL3.png": "975640a74690de806225275934f76546145d5ac332b8c59115617b1ce560eb6e",
  "/eu/hansolo/spacefx/levelBossL1.png": "a3bdad3f0362895e1e8de539a8f1af75ba911306b4af78735a4ed4b3e5a06a12",
  "/eu/hansolo/spacefx/levelBossL2.png": "a89c151f0f1cf3f2d44cdd5dc99128291675293a7bb537cfdaacaac89eb30fa7",
  "/eu/hansolo/spacefx/levelBossL3.png": "f4dbb76d1ff2f1279777ea49b24e65e490c979a51b8dac7c77596815fbb35f83",
  "/eu/hansolo/spacefx/levelBossRocket.mp3": "f9080ebb21cb465bda2495a1eb77a455a239c5a8867af87047d26b1b4a1d621e",
  "/eu/hansolo/spacefx/levelBossRocketL1.png": "98e4aa63bd3d9465db428a22b4a9c1819b3d48c8058defaf07b541c7d9d53352",
  "/eu/hansolo/spacefx/levelBossRocketL2.png": "a7a8d75f8e90efa5f7f0285d540e2464610a2cb9ccfea69200569ebf2f40d97c",
  "/eu/hansolo/spacefx/levelBossRocketL3.png": "e58360162b11c53fe0e2f7ba20ce273cfdaf145cc613a4f9d4388c7d210bc31a",
  "/eu/hansolo/spacefx/levelBossTorpedo.mp3": "aa886ecaba2030787c9a1fc1ac7fb25baab55fa4d3abb9a1ba4c47527de2ef93",
  "/eu/hansolo/spacefx/levelBossTorpedoL1.png": "1a33faa1bde64ffbcb0ea9eec1aeb794a23c2f7558cdabfe75b7ecc8e8405acb",
  "/eu/hansolo/spacefx/levelBossTorpedoL2.png": "7d8fff3e335293c2cbd69c2bbc6a4e895a57a2e11b808f66ee887f3809e88bfc",
  "/eu/hansolo/spacefx/levelBossTorpedoL3.png": "5a8e459eba89b7085dad2dea1f8cb5597ccaa1cacbdd3ee1166396fd6382bc42",
  "/eu/hansolo/spacefx/levelUp.mp3": "52b33ac0a096995d919e4f7b68579b917af90e5a717af2b8a6ac52a3cbaf04a0",
  "/eu/hansolo/spacefx/lifeUp.mp3": "ed4a153e5739273c76156d4d34c384146260a7959d240c9418e57aef959611e0",
  "/eu/hansolo/spacefx/lifeUp.png": "ebfcd7104a09b328788014d096dba76e14eef586c1c749328ac629f745723a90",
  "/eu/hansolo/spacefx/rainbowBlasterBonus.png": "419171a3e259008f598280244dd932dc8cdefb91c63d0a4b7007d0c444e3a8b6",
  "/eu/hansolo/spacefx/rainbowBlasterSound.mp3": "6491988582ed48ab32f924074a5bdd2ae9a63af29f912ee3b93a4371e0433d0d",
  "/eu/hansolo/spacefx/rocket.png": "9cb8f18f45c09a7aa66ba9932a7bef59f4aac4b184d4e8965f60f487100da403",
  "/eu/hansolo/spacefx/rocketExplosion.mp3": "94a78ca40ad0190dd837ef5a924e1c3173c51669ce3ca7b60919d3620b8e1840",
  "/eu/hansolo/spacefx/rocketExplosion.png": "2905503ebc073729366665db92ab82e78c4ee89b09deebb0e5cec1e1acfe49e3",
  "/eu/hansolo/spacefx/rocketLaunch.mp3": "86fdead6240cc6adc03c2811459a15610d03a46eec98cec3d3c5666baf3811c4",
  "/eu/hansolo/spacefx/shieldButton.png": "b2fe2b8e85e97cc11095063ea56a1f8fc150ef206d3662e2cfa9a4c56870c67e",
  "/eu/hansolo/spacefx/shieldUp.mp3": "04aaa6d62680f6f8e6c5f0cce93e7273f96f7ad2fa1374884cbe3731570c884a",
  "/eu/hansolo/spacefx/shieldUp.png": "a8fbb1432884088efe379cac3624b3dcbe8d4a39166b74dd37e7d96bcd9853ba",
  "/eu/hansolo/spacefx/shieldhit.mp3": "7601ae676854071b1c7932857d20ea7082500b9dc1e5f271be88cab285caf170",
  "/eu/hansolo/spacefx/spaceShipExplosionSound.mp3": "44eeddb954f0dc249787245df1a2244787ce890505c68aedbaecddbaa31c9eef",
  "/eu/hansolo/spacefx/spaceship.png": "34bd90bd33c6f87683f907d5dd95d77bbee9caa58bf4ad1ebfed0abdbf43bcba",
  "/eu/hansolo/spacefx/spaceshipDown.png": "55dab2348ab14aaf70b9bcf08452d0b31bfab1cb66ce916ee6c834b743288345",
  "/eu/hansolo/spacefx/spaceshipUp.png": "16558a4e47849dcb08cc170a0e5693e8b175796d49b17bafda4c8e4eac63b496",
  "/eu/hansolo/spacefx/spaceshipexplosion.png": "aa03ef68fe11a6f7fec356c1d77925cf49a9041a54ab9b84bb138b261baef19a",
  "/eu/hansolo/spacefx/speedUp.png": "58b06d0287f690cb56d296b191f7932e1c6c11d1d445e4883dc6c2c73b18941c",
  "/eu/hansolo/spacefx/starburst360Bonus.png": "d6307776e1ede234586f1144416e611c19e91a825a3406a6c89dd39836443c6d",
  "/eu/hansolo/spacefx/starburstBonus.png": "e0c9d258b34cbb4a065b024f03d2bf9702957a4745b0050919ab53aa9481425c",
  "/eu/hansolo/spacefx/startscreen.jpg": "73ccc486431bd2b48a9a0cc1b99b8a5b4fe8543091f520d706f6e26087b696d7",
  "/eu/hansolo/spacefx/startscreenIOS.jpg": "1d2ad2675794b80869ef36aa36b7dd117bb069f308290b2284c949e4d731d129",
  "/eu/hansolo/spacefx/torpedo.png": "76d0403f908df787474d6a4da4890493ed8006e7d169e62b668adfc241256ee7",
  "/eu/hansolo/spacefx/torpedoHit2.png": "2add099ef9128b2f33a405b72a2feefaa89eda5d7a5d5ef5b547b6754b70a69c",
  "/eu/hansolo/spacefx/torpedoHitL1.png": "d924560efd33af9f39c2334a8476dc9ffe5128e35199363afc39cae207e761e1",
  "/eu/hansolo/spacefx/torpedoHitL2.png": "f1ed60d12e4b68a662c13bcc20bbfb76ee54a3e4e36c4498eb5970494ba97685",
  "/eu/hansolo/spacefx/torpedoHitL3.png": "b12a488efc5b7f494f916ac4b9eaa93d76b55f6b0f4cf2f2dbac332b10db9aaa",
  "/eu/hansolo/spacefx/upExplosion.png": "73d0fa8806a5101be5f26f2e719f9994aa0f65412b060bab58c501715d2c2e77",
  "/index.html": "875f456583134deaa5d4286f501e83ffbfed6071b476fc1ff4bbe20ef0fed433",
  "/pwa-service-worker.js": "099ab5993a6a3eed7d715d6d3541b0e141c47298a864332274ba7f4caef9ffe4",
  "/webfx_demo_spacefx_application_gwt.devmode.js": "d9c40ea13de38a25b7db40c77ad7f65f4dc07abf021a3631e5c5f3f34fb382e8",
  "/webfx_demo_spacefx_application_gwt.nocache.js": "2733725d00b7c3c6de14eaa56f65b5ba8a3b0e7b6a93fe28c4acf32491b301c7"
};

function normalizeAsset(assetLike) {
    const hashToInfo = {};
    const pathToHash = {};
    try {
        for (const [path, v] of Object.entries(assetLike || {})) {
            let hash, preCache;
            if (typeof v === "string") { hash = v; preCache = DEFAULT_PRE_CACHE; }
            else if (v && typeof v.hash === "string") { hash = v.hash; preCache = (typeof v.preCache !== "undefined") ? !!v.preCache : DEFAULT_PRE_CACHE; }
            if (typeof path === "string" && hash) {
                hashToInfo[hash] = { path, preCache };
                pathToHash[path] = hash;
            }
        }
    } catch (e) { }
    return { hashToInfo, pathToHash };
}

const { hashToInfo: HASH_TO_INFO, pathToHash: PATH_TO_HASH } = normalizeAsset(ASSET);


// Build a cache Request for a given content hash
function toHashRequest(hash) {
    // Use a simple, prefix-free synthetic request URL based on scope + hash
    // This keeps keys unique per scope while avoiding the __asset_hash__/ prefix
    const scope = getScope();
    const u = scope + hash;
    return new Request(u);
}

// Remove any cached hash entries that are not present in the provided allowedHashes set
async function deleteHashesNotIn(allowedHashes, cache) {
    if (!cache) cache = await caches.open(CACHE_NAME)
    const keys = await cache.keys();
    let deletedCount = 0;
    await Promise.all(keys.map(async (req) => {
        // Only consider entries that were stored via toHashRequest(hash).
        // We assume such entries end exactly with the hash (scope + hash) and that hash is a 64-char hex string (sha-256)
        try {
            const url = new URL(req.url);
            const path = url.pathname || "";
            // scope path + 64 hex chars
            const scopePath = getScopePathname();
            const suffix = path.startsWith(scopePath) ? path.substring(scopePath.length) : null;
            const isHex64 = suffix && /^[a-f0-9]{64}$/i.test(suffix);
            const hash = isHex64 ? suffix : null;
            if (!hash) return;
            if (!allowedHashes.has(hash)) {
                const ok = await cache.delete(req);
                if (ok) deletedCount++;
            }
        } catch (e) { /* ignore parse errors */ }
    }));
    return deletedCount;
}

function getPathFromRequest(request) {
    try {
        const url = new URL(request.url);
        return url.pathname;
    } catch (e) {
        return request.url; // fall back (should already be a path for same-origin requests)
    }
}

function getScope() {
    return self.registration && self.registration.scope ? self.registration.scope : self.location.origin + "/";
}

function getScopePathname() {
    try {
        return new URL(getScope()).pathname;
    } catch (e) {
        return "/";
    }
}

function toScopedRequest(path) {
    // Ensure leading slash for consistency with manifest keys
    const p = path.startsWith("/") ? path : "/" + path;
    const scopedUrl = new URL(p, getScope()).toString();
    return new Request(scopedUrl);
}

function toManifestPathFromRequest(request) {
    const reqPath = getPathFromRequest(request) || "/";
    const scopePath = getScopePathname();
    if (scopePath && scopePath !== "/" && reqPath.startsWith(scopePath)) {
        const sub = reqPath.substring(scopePath.length);
        return "/" + (sub.startsWith("/") ? sub.substring(1) : sub);
    }
    return reqPath;
}

// Install: take control ASAP, actual caching is done during "activate" based on hash diff
self.addEventListener("install", event => {
    event.waitUntil(self.skipWaiting());
});

// Activate: ensure pre-cached assets are present (by hash) and remove stale hash entries
self.addEventListener("activate", event => {
    event.waitUntil((async () => {
        // Better to disable navigation preload for now
        if (self.registration.navigationPreload) {
            await self.registration.navigationPreload.disable();
        }
        const cache = await caches.open(CACHE_NAME);

        // Remove any cached hashes not present in current ASSET (best-effort)
        try {
            const allowed = new Set(Object.keys(HASH_TO_INFO));
            await deleteHashesNotIn(allowed, cache);
        } catch (e) { }

        // Pre-cache assets marked preCache=true
        await Promise.all(Object.entries(HASH_TO_INFO).filter(([, info]) => info && info.preCache === true).map(async ([hash, info]) => {
            const req = toHashRequest(hash);
            const existing = await cache.match(req);
            if (!existing) {
                try {
                    const resp = await fetch(info.path, { cache: "no-cache" });
                    if (resp && resp.ok) await cache.put(req, resp.clone());
                } catch (e) { }
            }
        }));

        // Become active immediately for open clients
        await self.clients.claim();
    })());
});

// Fetch: serve from cache first, then network as fallback
self.addEventListener("fetch", event => {
    console.log("Fetch request: " + event.request.url);
    event.respondWith((async () => {
        // Special case: the main page should be network-first (with cache fallback)
        try {
            let url2;
            try {
                url2 = new URL(event.request.url);
            } catch {
            }
            const sameOrigin2 = url2 && url2.origin === self.location.origin;
            const manifestPath2 = toManifestPathFromRequest(event.request);
            if (sameOrigin2 && (manifestPath2 === "/" || manifestPath2 === "/index.html")) {
                try {
                    const networkResponse = await fetch(event.request, {cache: "no-cache"});
                    if (networkResponse && networkResponse.ok) {
                        // Inspect the fetched index.html to detect version changes via mavenBuildTimestamp meta
                        try {
                            const clonedForMeta = networkResponse.clone();
                            const text = await clonedForMeta.text();
                            const match = text.match(/<meta\s+name=["']mavenBuildTimestamp["']\s+content=["']([^"']+)["']\s*\/?>(?:\s*<\/meta>)?/i);
                            if (match && typeof MAVEN_BUILD_TIMESTAMP !== "undefined") {
                                const fetchedTs = match[1];
                                if (fetchedTs !== MAVEN_BUILD_TIMESTAMP) {
                                    console.log("🔆🔆🔆🔆🔆 Detected index.html version change: fetched=" + fetchedTs + ", build=" + MAVEN_BUILD_TIMESTAMP);
                                    // Immediately fetch the new PWA manifest and clean caches for changed/removed assets
                                    try {
                                        const cleanupPromise = (async () => {
                                            try {
                                                const resp = await fetch("/pwa-asset.json", {cache: "no-cache"});
                                                if (resp && resp.ok) {
                                                    const json = await resp.json();
                                                    const newAsset = (json && (json.assetManifest || json)) || {};
                                                    const newHashes = new Set(Object.values(newAsset || {}).map(v => (typeof v === "string" ? v : (v && v.hash)) ).filter(Boolean));
                                                    const cache = await caches.open(CACHE_NAME);
                                                    const deletedCount = await deleteHashesNotIn(newHashes, cache);
                                                    if (deletedCount > 0) {
                                                        console.log("🧹 Cleaned " + deletedCount + " cached entries due to version change");
                                                    } else {
                                                        console.log("🧹 No cached entries required cleaning for version change");
                                                    }
                                                } else {
                                                    console.log("ℹ️ Could not fetch /pwa-asset.json (status " + (resp && resp.status) + ")");
                                                }
                                            } catch (eFetchMan) {
                                                console.log("ℹ️ Error while fetching/processing new manifest: " + (eFetchMan && eFetchMan.message ? eFetchMan.message : eFetchMan));
                                            }
                                        })();
                                        // Ensure the cleanup continues even if we return the response
                                        event.waitUntil(cleanupPromise);
                                    } catch (eCleanup) { /* ignore cleanup trigger errors */
                                    }
                                } else {
                                    console.log("✳️✳️✳️✳️✳️ index.html version still matches " + MAVEN_BUILD_TIMESTAMP);
                                }
                            }
                        } catch (eMeta) { /* ignore meta inspection errors */
                        }
                        try {
                            const cache = await caches.open(CACHE_NAME);
                            const h = PATH_TO_HASH[manifestPath2];
                            if (manifestPath2 === "/" || manifestPath2 === "/index.html") {
                                // Special: store index.html under its path key for offline fallback
                                await cache.put(toScopedRequest("/index.html"), networkResponse.clone());
                            } else if (h) {
                                await cache.put(toHashRequest(h), networkResponse.clone());
                            }
                        } catch (eCache) { /* ignore cache put errors */
                        }
                        return networkResponse;
                    }
                } catch (eNet) {
                    // Network failed; will try cache fallback below
                }
                const h2 = PATH_TO_HASH[manifestPath2];
                const fallback = (manifestPath2 === "/" || manifestPath2 === "/index.html")
                    ? (await caches.match(toScopedRequest("/index.html"))
                        || (h2 && await caches.match(toHashRequest(h2)))
                        || await caches.match(event.request)
                        || await caches.match(toScopedRequest(manifestPath2)))
                    : ((h2 && await caches.match(toHashRequest(h2))) || await caches.match(event.request) || await caches.match(toScopedRequest(manifestPath2)));
                if (fallback) {
                    return fallback;
                }
                // No cached main page; continue with the general strategy below
            }
        } catch (e) { /* ignore */
        }

        // 1) If same-origin and known asset, try by hash key first
        let url;
        try { url = new URL(event.request.url); } catch {}
        const sameOrigin = url && url.origin === self.location.origin;
        let cachedResponse;
        if (sameOrigin) {
            const manifestPath = toManifestPathFromRequest(event.request);
            const knownHash = PATH_TO_HASH[manifestPath];
            if (knownHash) {
                cachedResponse = await caches.match(toHashRequest(knownHash));
                if (cachedResponse) {
                    console.log("Found in cache by hash: " + knownHash + " for path " + manifestPath);
                    return cachedResponse;
                }
            }
        }

        // 2) Legacy fallbacks: try exact and scoped pathname (for compatibility with older caches)
        cachedResponse = await caches.match(event.request);
        if (cachedResponse) {
            console.log("Found in cache (legacy exact): " + event.request.url);
            return cachedResponse;
        }
        if (sameOrigin) {
            cachedResponse = await caches.match(toScopedRequest(url.pathname));
            if (cachedResponse) {
                console.log("Found in cache (legacy pathname): " + url.pathname);
                return cachedResponse;
            }
        }

        // 3) If this is a navigation-like request, attempt SPA fallbacks ('/' and '/index.html')
        const acceptsHtml = (() => {
            try {
                return (event.request.headers.get("accept") || "").includes("text/html");
            } catch {
                return false;
            }
        })();
        const isNavLike = (event.request.mode === "navigate")
            || (sameOrigin && (event.request.destination === "document"))
            || (sameOrigin && event.request.method === "GET" && acceptsHtml);
        if (isNavLike) {
            const candidates = ["/", "/index.html"];
            for (const p of candidates) {
                const h = PATH_TO_HASH[p];
                let resp = h ? await caches.match(toHashRequest(h)) : null;
                if (!resp) resp = await caches.match(toScopedRequest(p)); // legacy
                if (resp) {
                    console.log("📄 Serving navigation from cache: " + p);
                    return resp;
                }
            }
        }

        // 4) Network fallback with logging (and lazy cache-as-you-go)
        try {
            const networkResponse = await fetchWithRetry(event.request);
            console.log("✅ Fetch succeeded: " + event.request.url + " (status " + networkResponse.status + ")");
            // If this resource is marked for lazy caching, store it now under its hash key
            try {
                if (sameOrigin) {
                    const manifestPath = toManifestPathFromRequest(event.request);
                    const knownHash = PATH_TO_HASH[manifestPath];
                    const info = knownHash ? HASH_TO_INFO[knownHash] : null;
                    const isRange = isRangeRequest(event.request);
                    if (networkResponse && networkResponse.ok && info && info.preCache === false) {
                        if (!isRange && networkResponse.status !== 206) {
                            // Safe to cache the direct full response
                            const cache = await caches.open(CACHE_NAME);
                            await cache.put(toHashRequest(knownHash), networkResponse.clone());
                        } else {
                            // Partial content or Range request: background fetch full file once and cache it
                            try {
                                const p = backgroundFullFetchAndCache(manifestPath, knownHash);
                                // Ensure background operation can complete
                                event.waitUntil(p);
                            } catch (_) { /* ignore */ }
                        }
                    }
                }
            } catch (e2) {
                // ignore caching errors
            }
            return networkResponse;
        } catch (e) {
            console.error("❌ Fetch failed: " + event.request.url + " - " + (e && e.message ? e.message : e));
            // As a last resort for navigation-like requests, try cached index again
            if (isNavLike) {
                const resp = await caches.match(toScopedRequest("/index.html")) || await caches.match(toScopedRequest("/"));
                if (resp) return resp;
            }
            throw e;
        }
    })());
});


// Helper: detect Range requests (partial content)
function isRangeRequest(request) {
    try {
        const h = request && request.headers && request.headers.get ? request.headers.get("range") : null;
        return !!(h && h.trim());
    } catch (e) {
        return false;
    }
}

// Helper: fetch with a fallback retry using a reconstructed Request and no-store cache mode
async function fetchWithRetry(request) {
    try {
        return await fetch(request);
    } catch (e1) {
        try {
            const init = {
                method: request.method,
                headers: request.headers,
                mode: request.mode,
                credentials: request.credentials,
                cache: "no-store",
                redirect: "follow",
                integrity: request.integrity,
                referrer: request.referrer,
                referrerPolicy: request.referrerPolicy,
                keepalive: request.method === "GET" ? true : undefined,
                signal: request.signal
            };
            const retryReq = new Request(request.url, init);
            return await fetch(retryReq);
        } catch (e2) {
            throw e1;
        }
    }
}

// Helper: when a lazy asset was requested via Range or returned 206,
// fetch the full file in the background and cache it by content hash.
async function backgroundFullFetchAndCache(manifestPath, knownHash) {
    try {
        if (!knownHash) return;
        const cache = await caches.open(CACHE_NAME);
        // If already cached fully by hash, skip
        const existing = await cache.match(toHashRequest(knownHash));
        if (existing) return;
        // Fetch the full resource without Range constraints
        const fullReq = toScopedRequest(manifestPath);
        const resp = await fetchWithRetry(fullReq);
        if (resp && resp.ok && resp.status !== 206) {
            await cache.put(toHashRequest(knownHash), resp.clone());
        }
    } catch (e) {
        // best-effort: ignore errors
    }
}
