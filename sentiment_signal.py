"""
sentiment_signal.py
Gold (XAU/USD) news sentiment signal using Marketaux API.
"""

import requests

MARKETAUX_API_TOKEN = "qt8IAU0c9kpohAajF3ql5CeEfnF5cglRnD6mUGp3"
MARKETAUX_URL = "https://api.marketaux.com/v1/news/all"

BUY_THRESHOLD = 0.15
SELL_THRESHOLD = -0.15


def fetch_gold_news(limit=10, hours_back=24):
    params = {
        "api_token": MARKETAUX_API_TOKEN,
        "search": "gold OR XAU OR bullion OR \"precious metals\"",
        "language": "en",
        "limit": limit,
        "sort": "published_desc",
    }
    try:
        response = requests.get(MARKETAUX_URL, params=params, timeout=15)
        response.raise_for_status()
        data = response.json()
        return data.get("data", [])
    except requests.exceptions.RequestException as e:
        print(f"[sentiment_signal] API error: {e}")
        return []
    except ValueError as e:
        print(f"[sentiment_signal] JSON parse error: {e}")
        return []


def extract_sentiment_scores(articles):
    scores = []
    for article in articles:
        entities = article.get("entities", [])
        for entity in entities:
            score = entity.get("sentiment_score")
            if score is not None:
                scores.append(score)
    return scores


def get_sentiment_signal(limit=10, hours_back=24):
    articles = fetch_gold_news(limit=limit, hours_back=hours_back)

    if not articles:
        return {"signal": "WAIT", "avg_sentiment": 0.0, "article_count": 0, "headlines": []}

    scores = extract_sentiment_scores(articles)

    headlines = []
    for article in articles:
        title = article.get("title", "")
        entities = article.get("entities", [])
        entity_score = entities[0].get("sentiment_score") if entities else None
        headlines.append((title, entity_score))

    if not scores:
        return {"signal": "WAIT", "avg_sentiment": 0.0, "article_count": len(articles), "headlines": headlines}

    avg_sentiment = sum(scores) / len(scores)

    if avg_sentiment >= BUY_THRESHOLD:
        signal = "BUY"
    elif avg_sentiment <= SELL_THRESHOLD:
        signal = "SELL"
    else:
        signal = "WAIT"

    return {
        "signal": signal,
        "avg_sentiment": round(avg_sentiment, 4),
        "article_count": len(articles),
        "headlines": headlines,
    }


if __name__ == "__main__":
    result = get_sentiment_signal()
    print("Signal:", result["signal"])
    print("Avg sentiment:", result["avg_sentiment"])
    print("Articles analyzed:", result["article_count"])
    print("\nHeadlines:")
    for title, score in result["headlines"]:
        print(f"  [{score}] {title}")
