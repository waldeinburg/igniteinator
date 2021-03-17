import VisibilitySensor from "react-visibility-sensor";
import { getFullImageUrl } from "../model";
import "./CardImage.css";


const CardImage = ({ src }) => (
    <VisibilitySensor partialVisibility>
        {({ isVisible }) =>
            isVisible ? (
                <div><img src={getFullImageUrl(src)} alt="Card" /></div>
            ) : (
                <div className="unloadedCardImage"></div>
            )
        }
    </VisibilitySensor>
);

export default CardImage;
