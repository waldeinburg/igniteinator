import React from "react";
import VisibilitySensor from "react-visibility-sensor";
import { getFullImageUrl } from "../model";
import "./CardImage.css";

class CardImage extends React.Component {
    constructor(props) {
        super(props);
        this.state = { load: false };
    }

    onVisibilityChange = isVisible => {
        if (isVisible && !this.state.load)
            this.setState({ load: true })
    }

    render() {
        return (
            <VisibilitySensor partialVisibility onChange={this.onVisibilityChange}>
                {this.state.load ? (
                    <div><img src={getFullImageUrl(this.props.src)} alt="Card" /></div>
                ) : (
                    <div className="unloadedCardImage"></div>
                )}
            </VisibilitySensor>
        );
    }
}

export default CardImage;
