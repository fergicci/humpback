import React, { useEffect, useState } from "react";
import { Card } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import { TRANSLATION_KEYS, I18N_NAMESPACES } from "@/i18n/keys";
import { getBaseLang } from "@/utils/langUtils";
import { getNews, NewsItem } from "@/services/newsService";
import { SkeletonLoader } from "@/components/SkeletonLoader";

const NewsPanel: React.FC = () => {
  const { t, i18n } = useTranslation(I18N_NAMESPACES.HOME);
  const [newsItems, setNewsItems] = useState<NewsItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadNews = async () => {
      try {
        const lang = i18n.language;
        const news = await getNews(getBaseLang(lang), 1, 3);
        setNewsItems(news);
      } catch (error) {
        console.error("Error fetching news:", error);
      } finally {
        setIsLoading(false);
      }
    };

    loadNews();
  }, [i18n.language]);

  return (
    <Card className="shadow-sm">
      <Card.Header className="bg-dark text-white">
        {t(TRANSLATION_KEYS.HOME.NEWS_PANEL.TITLE)}
      </Card.Header>
      <Card.Body>
        {isLoading ? (
          <>
            <SkeletonLoader />
            <SkeletonLoader />
            <SkeletonLoader />
          </>
        ) : newsItems.length > 0 ? (
          <ul className="list-unstyled">
            {newsItems.map((item) => (
              <li key={item.id} className="mb-3">
                <div className="fw-bold">{item.title}</div>
                <div className="small text-muted">
                  {new Date(item.createdAt).toLocaleDateString()}
                </div>
                <div>{item.content}</div>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-muted">
            {t(TRANSLATION_KEYS.HOME.NEWS_PANEL.EMPTY)}
          </p>
        )}
      </Card.Body>
    </Card>
  );
};

export default NewsPanel;