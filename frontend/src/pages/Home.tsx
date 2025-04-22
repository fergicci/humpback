import { Container, Row, Col } from "react-bootstrap";
import NewsPanel from "@/components/NewsPanel";
import logo from "@/assets/humpback-logo.png";
import { useTranslation } from "react-i18next";
import { TRANSLATION_KEYS, I18N_NAMESPACES } from "@/i18n/keys";

function Home() {
  const { t } = useTranslation(I18N_NAMESPACES.HOME);

  return (
    <Container className="my-5">
      <Row>
        <Col md={8}>
          <div className="d-flex flex-column align-items-center justify-content-center text-center">
            <img
              src={logo}
              alt="Humpback Studio Logo"
              className="img-fluid responsive-logo"
            />
            <h3 className="display-6 mt-4">
              {t(TRANSLATION_KEYS.HOME.WELCOME.MESSAGE)}
            </h3>
            <p className="lead">{t(TRANSLATION_KEYS.HOME.WELCOME.GREETINGS)}</p>
          </div>
        </Col>

        <Col md={4}>
          <NewsPanel />
        </Col>
      </Row>
    </Container>
  );
}

export default Home;
