const MAVEN_BUILD_TIMESTAMP = "2025-11-16T17:31:42Z";

console.log("PWA mode is on - mavenBuildTimestamp = " + MAVEN_BUILD_TIMESTAMP);

const CACHE_NAME = "webfx-pwa-cache";
const DEFAULT_PRE_CACHE = false;

// Single asset map that PwaMojo will populate: { "/file1": { preCache: true|false, hash: "XXXX" }, "/file2": "YYYY", ... }
// If preCache is missing, or the value is a string (treated as a hash), we consider preCache = DEFAULT_PRE_CACHE
const ASSET = {
  "/270F1107CDA221154A5EFD3C4482D30B.cache.js": "af483757a4f8eb165269e82a71a7b490a517d5005e72f023849e4f0df5c29b79",
  "/dev/webfx/kit/css/main.css": "982ec3c996155374e1026b845bd45296a49ce5010341f4e58ac286f64230ba9d",
  "/dev/webfx/kit/perfectscrollbar/perfect-scrollbar.css": "7b6508c9e8e04de8ebfec5de2ce1c4303bc46a0a279283eff7e248c1c900a91b",
  "/dev/webfx/kit/perfectscrollbar/perfect-scrollbar.externs.js": "95a3f9084f5ca536168b60ffa143446e8ed6f9271111f92e4d0b0d56385c403a",
  "/dev/webfx/kit/perfectscrollbar/perfect-scrollbar.js": "dcdccf78523537e1188b5a173b48d497e243891f3aeddaf1e79eb5d10ee3088f",
  "/dev/webfx/kit/perfectscrollbar/perfect-scrollbar.min.js": "3535c240c3cfef15b9fd77b610c9b41db9ff2f4569949230b7f15a9c8cd0b6e7",
  "/emul/java/util/regex/xregexp.js": "263ed3cef90a59f0a94fbc2e54dfd0c32526df337132e361e0e9303502828f58",
  "/eu/hansolo/fx/tetris/Silkworm.ttf": "5bac9845a9dc198d720a372c610b169dfc911bcdf9171c874256a866ee2e3f64",
  "/eu/hansolo/fx/tetris/blueBlock.png": "e071d656f24fce7b5a08b512b6f667eddd32b97442bb13158873072c5989ccad",
  "/eu/hansolo/fx/tetris/blueGlossyBlock.png": "3c5c0b79455138dd44a589c19e8e87864eee9916330d10fd8b269fa9c77d942e",
  "/eu/hansolo/fx/tetris/cyanBlock.png": "83335bd7209986c7de3b6c9bf2bc1d0d09759d309097e48bb7c353c1345acc6a",
  "/eu/hansolo/fx/tetris/cyanGlossyBlock.png": "fd82fd27cc397e47b3eb1311eb1d4fcbeea32dea85a561150fe24cdce849b924",
  "/eu/hansolo/fx/tetris/githubDarkGreenBlock.png": "6e69ed3319027d3f505e5becdc1ea85b62aeaf445bf45de883eaafe6f5cb3586",
  "/eu/hansolo/fx/tetris/githubGreenBlock.png": "6fe7fcdba296d41e0ef5bc2c69890295274d6e7a80725c310b2afb8309da7951",
  "/eu/hansolo/fx/tetris/githubLightGreenBlock.png": "0e5fb17403e21ac418f8be8c24221dbdfec5d0be14dac8908376baebfc83df82",
  "/eu/hansolo/fx/tetris/githubVeryLightGreenBlock.png": "55ef8555e16b4899b53836850fd9b83b6111fcdb18de3592101b8c9b47627eed",
  "/eu/hansolo/fx/tetris/greenBlock.png": "67f13dc246f068eec67e4c7ae88c3ec8b95e1f22952f2d1ebabb0d48004faa7b",
  "/eu/hansolo/fx/tetris/greenGlossyBlock.png": "6d67d559975aaa511a6e35a72a01b8015b1648443cdd74f1e02436ade1ebedd0",
  "/eu/hansolo/fx/tetris/icon.png": "146ec72ab08f02df1ac81e3c2204627a0ff57d82842745e874651e0c6f96e853",
  "/eu/hansolo/fx/tetris/icon128x128.png": "c20c7edde9aba4f7fafc4284d50d7aba3a326dcac8b82c87234092f72aab9121",
  "/eu/hansolo/fx/tetris/icon16x16.png": "da9a5ce829084862ed41c2430a0bee8acac6798d65f40900717f4da0fe56fd41",
  "/eu/hansolo/fx/tetris/icon256x256.png": "82695ea342883558cb00018150fcad23b11cd49afe095125d8f3eaeab796b68b",
  "/eu/hansolo/fx/tetris/icon32x32.png": "53c3e5cf085ea7d9957ad7611ba884e952ac6f5020727d6563a42648ccc26613",
  "/eu/hansolo/fx/tetris/icon48x48.png": "00dc08703bb1f6f239975f024b2b1952c88dfa021bfb8e9d62b49bed9d4c70e6",
  "/eu/hansolo/fx/tetris/orangeBlock.png": "ace81fb73806d621b6fdefdae0a6c1a6919085bad46a9fe8f4a3a61c7e38d386",
  "/eu/hansolo/fx/tetris/orangeGlossyBlock.png": "fd7de084779c069464e668fa48b63e78afb853d180bacc3a1ee4adf6b6dfb5b2",
  "/eu/hansolo/fx/tetris/purpleBlock.png": "0eabab27a87da5036944ad7a256f83bcc91574603efecb7b0249fec1e4ad17bc",
  "/eu/hansolo/fx/tetris/purpleGlossyBlock.png": "ff9a33853f7d502ad444e11c1233ba0e7c644780012a801062bad56d0d584caa",
  "/eu/hansolo/fx/tetris/redBlock.png": "3c0bd5ce2e9f97c16f557f19cb991107a70de149463c0f1bfaf173a3d0b3092e",
  "/eu/hansolo/fx/tetris/redGlossyBlock.png": "350d08ada91e539840bfd6e6bedcc56e532c198fb8be6372f045e6c04dd628a6",
  "/eu/hansolo/fx/tetris/soundtrack.mp3": "63fcf315af202fab5cb51d832064c8bb0b02137baf680f752feff2c17708e5d8",
  "/eu/hansolo/fx/tetris/startScreen.png": "069ed8e37a319ddf0870df7b8c7a3d3c95c9e42fbe9590597a5a33485a4f6a6b",
  "/eu/hansolo/fx/tetris/tetris-4-lines.mp3": "378d38993608716165ab5d5b0564cbdb9e6c6a2b44f590b3bf95d8b133546e01",
  "/eu/hansolo/fx/tetris/tetris-block-falling.mp3": "5727c1405ec7011cc8a59dbba58effba9d88bd8b8dac0b694a22db77467f6b62",
  "/eu/hansolo/fx/tetris/tetris-block-landed.mp3": "217f3e40249a440baee40400efc624de7e01dee6e1d6f582972139b7daa2cc2f",
  "/eu/hansolo/fx/tetris/tetris-game-over.mp3": "f1308e7b88767866770ee64034fb4d9144a33f0bb7588ff5dd16bb4a3fabeced",
  "/eu/hansolo/fx/tetris/tetris-level-up-jingle.mp3": "636c3427f243a19dd4450a119b366ebb7056d7b329a37873db2b4165370f25d8",
  "/eu/hansolo/fx/tetris/tetris-line-clear.mp3": "97eab9bd2fa9367f32973b0b149978954c71b52d18934a3510708abc57007e9e",
  "/eu/hansolo/fx/tetris/tetris-move-block.mp3": "c2fa2470fbbc9b900925d3546cd4b9a523eb10df918009282d613b7945e3998f",
  "/eu/hansolo/fx/tetris/tetris-rotate-block.mp3": "124a70981c0ad048f7e67dad8b50fdf0e30f21460da3a3825075b1cd2fc4f3e3",
  "/eu/hansolo/fx/tetris/yellowBlock.png": "7eda55e1085c872ee8e62b76bbc11c6946a13ec70b8b91ecdf859b5d03ef6611",
  "/eu/hansolo/fx/tetris/yellowGlossyBlock.png": "5407f3f33d340e030045e842165d529ddc99340fb8b37b697dc537802e41782e",
  "/index.html": "1b03eecde2a1776fe6b3cd95f74139a58e76515946de9993736e98e4d61958e8",
  "/pwa-service-worker.js": "c2d72c1135ec499e2fe34f91702f6aff1dd04f407d1e5f47cde5f4a7f15ada91",
  "/webfx_demo_tetris_application_gwt.devmode.js": "60ed1c89bb731775964fc8e3ee72172895aaf30107fce391c8c198bb06ddb3ac",
  "/webfx_demo_tetris_application_gwt.nocache.js": "79ec05b92c173259d10bc6343e8a896da776c75abeffa56da189801b4397b2f4"
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
