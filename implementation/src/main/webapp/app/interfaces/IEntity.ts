export interface IEntity {
    label: string;
    id: string;
    URI: string;
    source: string;
    classes: string;
    
    description?: string;
    imageUrl?: string;
    isInstance?: boolean;
}