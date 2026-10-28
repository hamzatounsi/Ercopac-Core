package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.projectum_ai.service.OllamaAiService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

// Message translation for ticket threads and live chat, through the organisation's local Ollama model
// (same model and settings as the Projectum AI assistant). Results are cached in memory.
@Service
public class TicketTranslationService {
 private static final Map<String, String> NAMES = Map.of("en", "English", "fr", "French", "es", "Spanish", "it", "Italian", "pt", "Portuguese", "de", "German", "ar", "Arabic");
 private static final int MAX_CACHE = 2000;
 private final OllamaAiService ai;
 private final Map<String, String> cache = Collections.synchronizedMap(new LinkedHashMap<>(256, 0.75f, true) {
  @Override protected boolean removeEldestEntry(Map.Entry<String, String> eldest) { return size() > MAX_CACHE; }
 });

 public TicketTranslationService(OllamaAiService ai) { this.ai = ai; }

 public String translate(String text, String to) {
  if (text == null || text.isBlank()) return "";
  String target = NAMES.get(to == null ? "" : to.toLowerCase());
  if (target == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported language " + to + ".");
  if (text.length() > 2000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message too long to translate.");
  String key = to.toLowerCase() + "\u0000" + text;
  String cached = cache.get(key);
  if (cached != null) return cached;
  String translated = ai.ask("Translate the following customer support message into " + target + ". "
    + "If it is already in " + target + ", return it unchanged. Return only the translated text, without quotes or explanations.\n\nMessage:\n" + text).trim();
  if (translated.isEmpty()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The translation model returned no text.");
  cache.put(key, translated);
  return translated;
 }
}
