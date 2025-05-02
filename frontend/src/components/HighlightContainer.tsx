import React, { useState } from "react";
import { Modal, Card, Row, Col } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import { I18N_NAMESPACES, TRANSLATION_KEYS } from "@/i18n/keys";
import compositionBackground from "@/assets/home-comp-bg.png";
import recordingBackground from "@/assets/home-rec-bg.png";
import mixingBackground from "@/assets/home-mix-bg.png";
import masteringBackground from "@/assets/home-master-bg.png";
import workshopBackground from "@/assets/home-work-bg.png";
import shopBackground from "@/assets/home-shop-bg.png";

interface HighlightItem {
  id: string;
  title: string;
  description: string[];
  icon: string;
}

const HighlightContainer: React.FC = () => {
  const { t } = useTranslation(I18N_NAMESPACES.HOME);
  const [selected, setSelected] = useState<HighlightItem | null>(null);

  const highlights: HighlightItem[] = [
    {
      id: "composition",
      title: t(TRANSLATION_KEYS.HOME.HIGHLIGHT.COMPOSITION.TITLE),
      description: t(TRANSLATION_KEYS.HOME.HIGHLIGHT.COMPOSITION.DESCRIPTION, {
        returnObjects: true,
      }) as string[],
      icon: compositionBackground,
    },
    {
      id: "recording",
      title: t(TRANSLATION_KEYS.HOME.HIGHLIGHT.RECORDING.TITLE),
      description: t(TRANSLATION_KEYS.HOME.HIGHLIGHT.RECORDING.DESCRIPTION, {
        returnObjects: true,
      }) as string[],
      icon: recordingBackground,
    },
    {
      id: "mixing",
      title: t(TRANSLATION_KEYS.HOME.HIGHLIGHT.MIXING.TITLE),
      description: t(TRANSLATION_KEYS.HOME.HIGHLIGHT.MIXING.DESCRIPTION, {
        returnObjects: true,
      }) as string[],
      icon: mixingBackground,
    },
    {
      id: "mastering",
      title: t(TRANSLATION_KEYS.HOME.HIGHLIGHT.MASTERING.TITLE),
      description: t(TRANSLATION_KEYS.HOME.HIGHLIGHT.MASTERING.DESCRIPTION, {
        returnObjects: true,
      }) as string[],
      icon: masteringBackground,
    },
    {
      id: "workshop",
      title: t(TRANSLATION_KEYS.HOME.HIGHLIGHT.WORKSHOPS.TITLE),
      description: t(TRANSLATION_KEYS.HOME.HIGHLIGHT.WORKSHOPS.DESCRIPTION, {
        returnObjects: true,
      }) as string[],
      icon: workshopBackground,
    },
    {
      id: "shop",
      title: t(TRANSLATION_KEYS.HOME.HIGHLIGHT.SHOP.TITLE),
      description: t(TRANSLATION_KEYS.HOME.HIGHLIGHT.SHOP.DESCRIPTION, {
        returnObjects: true,
      }) as string[],
      icon: shopBackground,
    },
  ];

  return (
    <div className="my-5">
      <Row>
        {highlights.map((item) => (
          <Col key={item.id} md={4} className="mb-4">
            <Card
              className="highlight-card shadow-sm"
              onClick={() => setSelected(item)}
              role="button"
            >
              <Card.ImgOverlay className="d-flex align-items-start justify-content-center p-0">
                <div className="highlight-card-title w-100 text-center">
                  <h5 className="m-0">{item.title}</h5>
                </div>
              </Card.ImgOverlay>
              <Card.Img
                src={item.icon}
                alt={item.title}
                className="highlight-card-img"
              />
            </Card>
          </Col>
        ))}
      </Row>

      <Modal show={!!selected} onHide={() => setSelected(null)} centered>
        <Modal.Header
          closeButton
          className="bg-dark text-white rounded-top px-4 py-3 d-flex align-items-center"
        >
          <Modal.Title className="navbar-brand mb-0 fs-5 lh-sm">
            {selected?.title}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selected?.description.map((paragraph: string, index: number) => (
            <p key={index}>{paragraph}</p>
          ))}
        </Modal.Body>
      </Modal>
    </div>
  );
};

export default HighlightContainer;
