import React from "react";
import { Card } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import { TRANSLATION_KEYS, I18N_NAMESPACES } from "@/i18n/keys";

interface NewsItem {
  id: string;
  date: string;
  message: string;
}

interface NewsPanelProps {
  newsItems?: NewsItem[];
}

const NewsPanel: React.FC<NewsPanelProps> = ({ newsItems = [] }) => {
  const { t } = useTranslation(I18N_NAMESPACES.HOME);

  return (
    <Card className="shadow-sm">
      <Card.Header className="bg-dark text-white">
        {t(TRANSLATION_KEYS.HOME.NEWS_PANEL.TITLE)}
      </Card.Header>
      <Card.Body>
        {newsItems.length > 0 ? (
          <ul className="list-unstyled">
            {newsItems.map((item) => (
              <li key={item.id}>{item.message}</li>
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