import React from "react";
import { Container } from "react-bootstrap";
import GearIntroPanel from "@/components/GearIntroPanel";

const Gear: React.FC = () => {
  return (
    <Container className="my-5">
      <GearIntroPanel />
      {/* future gear list or images */}
    </Container>
  );
};

export default Gear;
