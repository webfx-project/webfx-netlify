const MAVEN_BUILD_TIMESTAMP = "2025-11-16T17:32:10Z";

console.log("PWA mode is on - mavenBuildTimestamp = " + MAVEN_BUILD_TIMESTAMP);

const CACHE_NAME = "webfx-pwa-cache";
const DEFAULT_PRE_CACHE = false;

// Single asset map that PwaMojo will populate: { "/file1": { preCache: true|false, hash: "XXXX" }, "/file2": "YYYY", ... }
// If preCache is missing, or the value is a string (treated as a hash), we consider preCache = DEFAULT_PRE_CACHE
const ASSET = {
  "/C301915466C42966367160AF1B3935A9.cache.js": "41541627cca2967a4b870b2a9a1bc7381c54e0d4f17dc633ac0f5182a51d9f36",
  "/dev/webfx/kit/css/main.css": "42212f96bf8547bc9513d7fe75ef0e90c04d2b22db8c8cf621ca0a00dbf9d1ac",
  "/emul/java/util/regex/xregexp.js": "263ed3cef90a59f0a94fbc2e54dfd0c32526df337132e361e0e9303502828f58",
  "/eu/hansolo/fx/jarkanoid/Emulogic-zrEw.ttf": "0f0b99c1404ba7736cbf2eabcd8772d23fe06e8fd9dc0a2f280204ded9577db4",
  "/eu/hansolo/fx/jarkanoid/backgroundPattern_1.png": "0615fd799cd8cf356f8cb4eaa78fc692e0cb4e2c1ec712953b742edfe6fc9b8b",
  "/eu/hansolo/fx/jarkanoid/backgroundPattern_2.png": "93f8b436f0accebcb9b8b19e9d9e99df5c0f543bfaa81db15da2627a77c4cc68",
  "/eu/hansolo/fx/jarkanoid/backgroundPattern_3.png": "b9a4f5b32fdd863e79aa124cc769b3156483de0397f8e41338f308f79ebaa222",
  "/eu/hansolo/fx/jarkanoid/backgroundPattern_4.png": "235b734eb8e499bca6fbfe80a38b506fcaf432a54d640fce42f4612a18a1d6bf",
  "/eu/hansolo/fx/jarkanoid/ball.png": "069ef3dac601a969de8d5451a00bbdc4faaf6763d5d4a92e2b75c8a5c3955be2",
  "/eu/hansolo/fx/jarkanoid/ball_block.mp3": "eb986f0248fa29442716e1dbf640af36ff6e02ad0c666e081c823d865825e2ee",
  "/eu/hansolo/fx/jarkanoid/ball_hard_block.mp3": "41741868e7942907b4614794567e02b74884e492ba5ead0e089da570ad11a4a0",
  "/eu/hansolo/fx/jarkanoid/ball_paddle.mp3": "1b7197b3506911f7ec8efffe4ac6e9abf9dd6808fe81c9deeba6158b8fa4654f",
  "/eu/hansolo/fx/jarkanoid/ball_shadow.png": "cdf5d8fbcfa0d5d943ac95fc0e3a231dbb5a6cfce57ee8f7b1772765bb857dec",
  "/eu/hansolo/fx/jarkanoid/blink_map.png": "77a48c1eb4c553061dbb9d2fed6a00585fb81089ea4a3617ad19bf363769a2a4",
  "/eu/hansolo/fx/jarkanoid/block_gray.png": "d6ffa2d6cb433c65a284ed020ddef75ab33806e69824efb78791ede79936fbfc",
  "/eu/hansolo/fx/jarkanoid/block_map_bonus_b.png": "52e4629b4158ed5864d353ce3ac6f6a97f02129a4b54a0e3936ed033b44c21dc",
  "/eu/hansolo/fx/jarkanoid/block_map_bonus_c.png": "3033e5d07f92a5f001a68a813bde7caaa883be13b4c94acf045c3e5b4bc0e5c4",
  "/eu/hansolo/fx/jarkanoid/block_map_bonus_d.png": "133fbb57dee21c6513b3b7051a0ff0ff3bcbdb1c079438f19d91280cd39ea012",
  "/eu/hansolo/fx/jarkanoid/block_map_bonus_f.png": "bf55056cd3a990568bcd52e8d1ffeedd7ece1a32e8ef793df4bbda2dee4852ab",
  "/eu/hansolo/fx/jarkanoid/block_map_bonus_l.png": "73f042e7ec1744c8c8a4fc90d4f258d629f2ca8d4b7d4cf506ff3cc7ec87905e",
  "/eu/hansolo/fx/jarkanoid/block_map_bonus_p.png": "e8c21e9505844b62e0e65e1c90a572d4a2584e0e370500b7192f8a6666a5b81f",
  "/eu/hansolo/fx/jarkanoid/block_map_bonus_s.png": "3b676530e8d2301215fdf1b47636bf330f2904fcd627ac81165a05c7c9a01e79",
  "/eu/hansolo/fx/jarkanoid/block_shadow.png": "57aa74474458a94aecfbe5fc3c43151f88731a899d1845e38a96b9a1a7f8ccf9",
  "/eu/hansolo/fx/jarkanoid/blueBlock.png": "26b72718bebd1b2ea73c3fb102ea9a54d0315ff515127a9476c689a7e5b1ae8d",
  "/eu/hansolo/fx/jarkanoid/bonus_block_shadow.png": "fb56c1b84d7db0d0044d08f9c6f367cd29c49118d27d36adc53962edbcbd25af",
  "/eu/hansolo/fx/jarkanoid/borderPartVertical.png": "51e8265934b3775db271bc285dede2f6e588f67d14bef78d2dae457586a0f1da",
  "/eu/hansolo/fx/jarkanoid/borderPattern.png": "bafb8f321700956c21b8d053fda1f9f8d1741fcf86431d5ddb5814eba1f0b5ae",
  "/eu/hansolo/fx/jarkanoid/borderVertical.png": "d06b96102370ab4b21efcdb09b9716319edb9f8fb4601dae6660cd0fe5d3169d",
  "/eu/hansolo/fx/jarkanoid/copyright.png": "afa46572640bf98d622cae5daddd7bc4a73999ae49263057a5982c44f39cdcc2",
  "/eu/hansolo/fx/jarkanoid/cyanBlock.png": "58679bf8a151fd1de93e33c59871d357aed1675e8da5ba20e75d9f9f0f2b95ff",
  "/eu/hansolo/fx/jarkanoid/explosion.mp3": "09fed62ad839d18f6bb7579272c75b2d1dbf6e5fc939bd14a66a54937cdfed37",
  "/eu/hansolo/fx/jarkanoid/explosion_map.png": "f426bc5d88fefadf0b74310a10ef95e45b23ea1ae2cd4b228f4b5b4603f9aeb4",
  "/eu/hansolo/fx/jarkanoid/game_over.mp3": "c5d3c4267a13a7d9c59b935763062a36eb2ff08fe88038967dbf6e16ccaf1b6f",
  "/eu/hansolo/fx/jarkanoid/game_start.mp3": "ca396c3e2d1efe0054839f0d3281d7a3a8709be2f5550734a31a3f07a548b421",
  "/eu/hansolo/fx/jarkanoid/goldBlock.png": "92035678afc1c2feebce2739716a4ceebe5ca3480d181a73dc152e44e9d92890",
  "/eu/hansolo/fx/jarkanoid/grayBlock.png": "e5798dd5cfd91d586e1e90e98361c232693adac0e7093382e9af2496650863e7",
  "/eu/hansolo/fx/jarkanoid/greenBlock.png": "3c2b7185de8945cd0d6c0ffb1e1839df540951d6079e2004f7ad3e6a621a8114",
  "/eu/hansolo/fx/jarkanoid/gun.mp3": "a7261160f0ea2239ba4b068890c6c94d8ff2abef1d8d3b712b080505e3bced02",
  "/eu/hansolo/fx/jarkanoid/icon128x128.png": "c859530092c554163fda13c0e3cb76842618180e978379dc3d0e0e546409bc48",
  "/eu/hansolo/fx/jarkanoid/icon16x16.png": "074f98b80b9ba9b36af29253d32ab147cf7bb242cce0d081815ad641126ca98a",
  "/eu/hansolo/fx/jarkanoid/icon256x256.png": "98afa905aef52647da89b11180ace091b81f24a9890b04dab8a97b4de8f4ed52",
  "/eu/hansolo/fx/jarkanoid/icon32x32.png": "86dfcc284c41b633e6777fd318c95672875879e7499a0c4f7eb2a9abf0c14480",
  "/eu/hansolo/fx/jarkanoid/icon48x48.png": "b4806b36dbe4074cfb0a62c764bc8737e99c60ac4f2dc959dad760cf2bf4bb06",
  "/eu/hansolo/fx/jarkanoid/jarkanoid_logo.png": "b151e92eb8bed3e45bbdc0f48c61c0fd8fe72a6f88ba9b1fa86f2dbdcbe24fe1",
  "/eu/hansolo/fx/jarkanoid/level_ready.mp3": "378e775e4e45d998ba2591952b0b90ee56d0250f93aacdcfb9142ae4a75f0075",
  "/eu/hansolo/fx/jarkanoid/limeBlock.png": "43aae2c9d6a71abd56df8effe403dade40499862e88384d83aacb4033aae742b",
  "/eu/hansolo/fx/jarkanoid/magentaBlock.png": "211b92a46cc507d8efffafa65116360f260c6d06f6ef8942248636ac7af9c9b4",
  "/eu/hansolo/fx/jarkanoid/molecule_map.png": "272d88afc01d84846982c486f236007f6db229fb207d8e7e594ec2627b93a3e7",
  "/eu/hansolo/fx/jarkanoid/open_door_map.png": "dcae5881e43856053baa180784fe81122c574213bd1a61db870efcc9b6f6c3d3",
  "/eu/hansolo/fx/jarkanoid/orangeBlock.png": "84204941fe901c5b86a75c9be7cf03037c8b39ce66b9b24b13f75cf362a1e9d0",
  "/eu/hansolo/fx/jarkanoid/paddle_gun.png": "3c1ae6d17b4d03934a8084176138c9f869fcc202723c6ccaf314e82e021a8c56",
  "/eu/hansolo/fx/jarkanoid/paddle_gun_shadow.png": "97c3e18e86baf21cf7778aafc06c5b7a26c764001f93f788404ebaaef47361ef",
  "/eu/hansolo/fx/jarkanoid/paddle_std.png": "aaa70ed0afd833ba0e3a67baf0f4cb2be7f36d391b3befcdf1e81bcb2a080fa4",
  "/eu/hansolo/fx/jarkanoid/paddle_std_shadow.png": "fcbbc40d45287d2015f1eaff012557f2647cb05a0aad3eab76c38e7bf9c80de4",
  "/eu/hansolo/fx/jarkanoid/paddle_wide.png": "974be7a03deddefe455079db87abc5003fdd741a6409a4aad71e18253bd6390b",
  "/eu/hansolo/fx/jarkanoid/paddle_wide_shadow.png": "c171b09c4c0468ae0ea76b151b6a11f42203dcfddae5697c318146f1046253a4",
  "/eu/hansolo/fx/jarkanoid/paddlemap_gun.png": "8f01aa548e1f84dce9737c571345daa58b4e3a91750b7f89449016111cf4d04f",
  "/eu/hansolo/fx/jarkanoid/paddlemap_std.png": "5dafb046e7bceeb1c6f06ae54d2bf4c5da485623fdbac5541b7ba551aeee4b19",
  "/eu/hansolo/fx/jarkanoid/paddlemap_wide.png": "7a990a50cbe24b1d60c94fc64b64b25f4b19d5a3bb67b707b10c5243d71149e0",
  "/eu/hansolo/fx/jarkanoid/pipe.png": "e301591dfd6d3aa0826aad05171c11f1813eeb08adb112556e1bfcf38089b3ab",
  "/eu/hansolo/fx/jarkanoid/redBlock.png": "39e8e65eedcbe6a8600edf57596b943e3559ea51718f1cf9769835d813f3bead",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level01.png": "7e0bda3a756549bf46a876908535ae3f24e24fc1e3485c83e1ea756b660c13f9",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level02.png": "c05dbcd4340a54732ac07f29d03739d4dafcc45d0b5047381367084d4348cd28",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level03.png": "af32d5a31edd3ed28db90d08ea6e89ca13cf25f1f9b669459107525a601a78d8",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level04.png": "4c1c525ab887ca270574edcd0ccbd81f156e32eb14bc1f4ab6552419b26ca597",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level05.png": "2947983c23affe3ebcd67bb7a28405b706155cf4873d2b47ed1ac766925f3736",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level06.png": "bcac26915abdff3eb64b145b2e65a9f9e409ff07131abe3e138120a9e205ea8c",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level07.png": "7e9bf1e974a79151ca920d782b62f25805299e0071882de992ae5eddee711ecd",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level08.png": "7dff58b2e590ccd0c3ce1d01cfbc0c58498d28fe0575f838c5d12c00323b49fc",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level09.png": "f853073fce87370f293adefc03f696737dd8af57d39c115a0a128a0ca9ae5994",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level10.png": "9348f79f45b8de677bbde272692cbd808c07d166ef8e4692bab9860df57bd3ac",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level11.png": "b321d99bb9279458d8570f630cceabdb4ba2f3d6c9faaa0fcd0fce6d83519f9b",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level12.png": "d5a406858c671677ce89a56cf6a303d8bbda1b6c82e874794dd7ec8c540be092",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level13.png": "7c165024db1ffaee19ca52a5c388f20a68f40d171a7b8dc7a2fe2632f5d6e53a",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level14.png": "158601dcaadd6cac12130ca0b15393429aa748ca357a1472812091c78468e300",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level15.png": "9ce1dbfc239a9b0a6d756cd8e199413f3e32dba3f2b9dc419c851ce08327de5b",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level16.png": "49f9e78e8330000e04a80fb85b2286dbeb548bb254f4d2957f3d5247a9696d9a",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level17.png": "2115e6a1c0f596c97c328e775c5c24770c9a01e74dbc745717c32acee4138f09",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level18.png": "55532b71283eb64f97e2f321c1c7d6cbf863468c44295ab115784ebcfc04f4dd",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level19.png": "4ed8b14671001ab377fde827ad0f7cfee82fb1be03fba50e83ea34fc6db24844",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level20.png": "2c5241ec1030ba53ea83f4164b4ca1016934df91df96b45b1ca3e06dbe824880",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level21.png": "fbc6152e7859dc0a52954597ba48b9258660ececf29646b2844e1935d8462e9a",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level22.png": "6535246730bed5b18f87c364c98cd2fdeb6e3088a79808b12455cea6178a7841",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level23.png": "159985c8ee6a61865588192dd9e0d4135cabfc687fa1b422655ec985df28620e",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level24.png": "4e93ac3e60b97ab695a5fdf71e6ea3263abb1dd8a448f664d2355a6ad14c13bc",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level25.png": "19537f833c08b2b451dbf313fb981d4d05556c257e69c8b05095091df1dd81ab",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level26.png": "b1952a4ca9e5f89d68e8e6f5974269eba591dff8156f9d935e2186b006df0fa3",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level27.png": "5d435acac35b1a2502325eb1d84fee781e993a8df0558d04bdc293e7e937e72f",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level28.png": "5b3f9df4ce5ecf217bdf45cc1c228636e23857a2ac905c01ae96903656a5382b",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level29.png": "09cd1fb658471d5179720915cf4fc5cd20f374a004f19e2010b911cc4745a185",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level30.png": "49c10dff98ff1f8856a9e4e6679ae659a6245af6d0a7c5d723d51760bff3741c",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level31.png": "6fb6687ef5cb6c4a5776795b63d22ee0997cb803760e9b3d228f51e2a2d78735",
  "/eu/hansolo/fx/jarkanoid/thumbs/Level32.png": "71c74fab99f90e2bdda668acbd618514c47304ff83c9d22e3d0ea8f28be03da8",
  "/eu/hansolo/fx/jarkanoid/topDoor.png": "55c745cbdf7ce5f4082d858c36864511487d84e2d4f59f1edc6ccc9614c7263b",
  "/eu/hansolo/fx/jarkanoid/torpedo.png": "159a5caafd5b765b9d6baff3dd4781bb5ae0763b2295147b7b50a6521f502f3b",
  "/eu/hansolo/fx/jarkanoid/upperLeftCorner.png": "cace5d4e74f0b6cfaaa40da7d782b6ffbac63871f096a1b7c5980a5b9fd477ef",
  "/eu/hansolo/fx/jarkanoid/upperRightCorner.png": "d360255ac2c1fdbc0a155bf623d506b09821c89a37f2bcb1d6e78b88f196b4ff",
  "/eu/hansolo/fx/jarkanoid/whiteBlock.png": "b7cc7b506da36aee3e51e261816843d750f153ae36cdbe6a0164ca06153eb9e8",
  "/eu/hansolo/fx/jarkanoid/yellowBlock.png": "f34858abaef3166ee219ab8819c36200c97cb3e8a9a3f6dfe2c651b1be9dbf0a",
  "/index.html": "6d0ccb9c5920e0ea5a18d9a1bf95b28b5f3194198382dc672f516cebfaa6165a",
  "/pwa-service-worker.js": "00aa0aaab3460e9e7a0cc4b7aa24cbb2fcb16129a7009595c6e6b1c3bcefd856",
  "/webfx_demo_jarkanoid_application_gwt.devmode.js": "580d74dbf2447eb173b0a3fdc6df66da4edef49c18fc65c7b9e6732dc527c4d2",
  "/webfx_demo_jarkanoid_application_gwt.nocache.js": "8aa6f3757cb79ebc8a968f15870dbd0db9411ef26860d5d712dccfc9d8117149"
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
