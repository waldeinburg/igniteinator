import { getFullImageUrl } from "../model";

const Card = props => {
    const { card } = props;
    return (
        <div>
            <div>{card.name}</div>
            <div>{card.cost}</div>
            {/* <div><img src={getFullImageUrl(card.image)} alt="Card" /></div> */}
        </div>
    );
};

export default Card;
